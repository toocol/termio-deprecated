#![allow(dead_code)]
use std::cell::Cell;
use std::cell::RefCell;
use std::rc::Rc;
use std::slice;
use std::time::Duration;

use crate::native::native_adapter::*;
use gtk::gdk_pixbuf::Pixbuf;
use gtk::glib;
use gtk::glib::clone::Downgrade;
use gtk::glib::once_cell::sync::Lazy;
use gtk::glib::timeout_add_local;
use gtk::glib::Bytes;
use gtk::glib::Object;
use gtk::glib::ParamSpec;
use gtk::glib::ParamSpecInt;
use gtk::prelude::*;
use gtk::subclass::prelude::*;
use gtk::DrawingArea;
use gtk::Picture;
use utilities::TimeStamp;

glib::wrapper! {
    pub struct NativeNodeObject(ObjectSubclass<NativeNode>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

static mut REC_BYTES: [u8; 1280 * 800 * 4] = [0u8; 1280 * 800 * 4];

pub struct NativeNode {
    pub drawing_area: RefCell<Option<DrawingArea>>,
    pub picture: RefCell<Option<Picture>>,
    pub pixel_buffer: RefCell<Option<Pixbuf>>,
    pub key: Cell<i32>,
    pub width: Cell<i32>,
    pub height: Cell<i32>,

    still_connect: Cell<bool>,
    is_verbose: Cell<bool>,
    hibpi_aware: Cell<bool>,
    button_state: Cell<i32>,
    locking_error: Cell<bool>,

    fps_counter: Cell<i32>,
    frame_timestamp: Cell<u64>,
    num_values: i32,
    fps_values: RefCell<[f64; 10]>,
}

impl Default for NativeNode {
    fn default() -> Self {
        Self {
            drawing_area: RefCell::new(None),
            picture: RefCell::new(None),
            pixel_buffer: RefCell::new(None),
            key: Cell::new(-1),
            width: Cell::new(0),
            height: Cell::new(0),
            still_connect: Cell::new(false),
            is_verbose: Cell::new(false),
            hibpi_aware: Cell::new(false),
            button_state: Cell::new(0),
            locking_error: Cell::new(false),
            fps_counter: Cell::new(0),
            frame_timestamp: Cell::new(0),
            num_values: 10,
            fps_values: RefCell::new([0.0; 10]),
        }
    }
}

impl NativeNodeObject {
    pub fn new() -> Self {
        Object::new(&[])
    }

    pub fn buffer_changer(&self) -> bool {
        unsafe {
            let imp = self.imp();
            let mut flag = false;
            if let Some(ref pb) = *imp.pixel_buffer.borrow() {
                let bytes = pb.pixels();
                for i in 0..1280 * 800 * 4 {
                    if bytes[i] != REC_BYTES[i] {
                        flag = true;
                    }
                }
            }
            flag
        }
    }

    pub fn draw_snapshot(&self) {}

    pub fn cairo_draw_area(&self, pixbuf: Pixbuf) {
        if let Some(ref area) = *self.imp().drawing_area.borrow() {
            area.set_draw_func(move |_, cr, _width, _height| {
                GdkCairoContextExt::set_source_pixbuf(
                    cr,
                    &pixbuf,
                    pixbuf.width() as f64,
                    pixbuf.height() as f64,
                );
                cr.paint().expect("Invalid cario surface state.");
            });
        }
    }

    fn update_native_buffered_picture(&self) -> Option<&Self> {
        let imp = self.imp();
        let mut flag = false;
        let current_timestamp = TimeStamp::timestamp();
        let key = imp.key.get();
        imp.locking_error.set(!native_lock(key));
        if imp.locking_error.get() {
            return None;
        }

        let dirty = native_is_dirty(key);
        let is_ready = native_is_buffer_ready(key);

        native_process_native_events(key);

        if !dirty || !is_ready {
            native_unlock(key);
            return None;
        }

        let current_w = native_get_w(key);
        let current_h = native_get_h(key);

        let picture_w = match *imp.picture.borrow() {
            Some(ref picture) => picture.width(),
            None => 0,
        };
        let picture_h = match *imp.picture.borrow() {
            Some(ref picture) => picture.height(),
            None => 0,
        };
        // let picture_w = match *imp.drawing_area.borrow() {
        //     Some(ref picture) => picture.width(),
        //     None => 0,
        // };
        // let picture_h = match *imp.drawing_area.borrow() {
        //     Some(ref picture) => picture.height(),
        //     None => 0,
        // };
        println!(
            "picture cw:{}, ch:{}, w:{}, h:{}",
            current_w, current_h, picture_w, picture_h
        );

        if None == *imp.picture.borrow() || picture_w != current_w || picture_h != current_h {
            if imp.is_verbose.get() {
                println!(
                    "[{}]> -> new image instance, resize W: {}, H: {}",
                    key, current_w, current_h
                );
            }
            // When resize, unparent the old picture.
            if let Some(ref picture) = *imp.picture.borrow() {
                println!("[{}] -> Unparent the old picture.", key);
                picture.unparent();
            }

            unsafe {
                let buffer = slice::from_raw_parts(
                    native_get_buffer(key),
                    (current_w * current_h * 4) as usize,
                );
                REC_BYTES.copy_from_slice(buffer);
                let pixbuf = Pixbuf::from_bytes(
                    &Bytes::from_static(buffer),
                    gtk::gdk_pixbuf::Colorspace::Rgb,
                    true,
                    8,
                    current_w,
                    current_h,
                    current_w * 4,
                );

                let drawing_area = DrawingArea::builder()
                    .content_width(current_w)
                    .content_height(current_h)
                    .focus_on_click(true)
                    .build();
                // self.cairo_draw_area(pixbuf);
                // drawing_area.queue_draw();

                let picture = Picture::for_pixbuf(&pixbuf);
                picture.set_can_shrink(false);
                picture.set_halign(gtk::Align::Start);
                picture.set_valign(gtk::Align::Start);

                imp.picture.borrow_mut().replace(picture);
                imp.pixel_buffer.borrow_mut().replace(pixbuf);
                imp.drawing_area.borrow_mut().replace(drawing_area);
                flag = true;
            }
        } // Process if picture is None, or window size was changed.

        // println!("Buffer changed: {}", self.buffer_changer());

        // Have update the image, not dirty anymore
        native_set_dirty(key, false);
        let width = imp.width.get();
        let height = imp.height.get();
        let scale_factor = 1.0;
        if (width as f64 != native_get_w(key) as f64 / scale_factor
            || height as f64 != native_get_h(key) as f64 / scale_factor)
            && width > 0
            && height > 0
        {
            if imp.is_verbose.get() {
                println!(
                    "[{}]> requesting buffer resize W: {}, H: {}",
                    key, width, height
                );
            }
            native_resize(
                key,
                width * scale_factor as i32,
                height * scale_factor as i32,
            );
        }
        native_unlock(key);

        if imp.is_verbose.get() {
            let duration = current_timestamp - imp.frame_timestamp.get();
            let fps = (1e9 as f64) / (duration as f64);
            imp.fps_values.borrow_mut()[imp.fps_counter.get() as usize] = fps;
            if imp.fps_counter.get() == imp.num_values - 1 {
                let mut fps_average = 0.0;
                for fps_val in imp.fps_values.borrow().iter() {
                    fps_average += fps_val;
                }
                fps_average /= imp.num_values as f64;
                imp.fps_counter.set(0);
                println!("[{}]> fps: {}", key, fps_average);
            }
            imp.fps_counter.set(imp.fps_counter.get() + 1);
            imp.frame_timestamp.set(current_timestamp);
        }

        if flag {
            Some(self)
        } else {
            None
        }
    }
}

#[glib::object_subclass]
impl ObjectSubclass for NativeNode {
    const NAME: &'static str = "NativeNode";

    type Type = NativeNodeObject;

    type ParentType = gtk::Widget;
}

impl ObjectImpl for NativeNode {
    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> = Lazy::new(|| {
            vec![
                ParamSpecInt::builder("width").build(),
                ParamSpecInt::builder("height").build(),
            ]
        });

        PROPERTIES.as_ref()
    }

    fn set_property(&self, _id: usize, value: &glib::Value, pspec: &ParamSpec) {
        match pspec.name() {
            "width" => {
                let width = value
                    .get()
                    .expect("`NativeNode` width needs to be of type `i32`.");
                self.width.set(width);
                println!("Set node width: {}", width);
            }
            "height" => {
                let height = value
                    .get()
                    .expect("`NativeNode` height needs to be of type `i32`.");
                self.height.set(height);
                println!("Set node height: {}", height);
            }
            _ => unimplemented!(),
        }
    }

    fn property(&self, _id: usize, pspec: &ParamSpec) -> glib::Value {
        match pspec.name() {
            "width" => self.width.get().to_value(),
            "height" => self.height.get().to_value(),
            _ => unimplemented!(),
        }
    }
}

impl WidgetImpl for NativeNode {}

pub trait NativeNodeImpl {
    const CONNECTION_NAME: &'static str;

    fn rc(&self) -> Rc<RefCell<NativeNodeObject>>;

    fn connect<T>(&self, set_parent: T)
    where
        T: Fn(*const Picture, *const DrawingArea) + 'static,
    {
        let node_rc = self.rc();
        let weak_node = node_rc.downgrade();

        if node_rc.borrow().imp().key.get() < 0
            || !native_is_connected(node_rc.borrow().imp().key.get())
        {
            node_rc
                .borrow()
                .imp()
                .key
                .set(native_connect_to(Self::CONNECTION_NAME));
        }
        node_rc.borrow().imp().still_connect.set(true);

        timeout_add_local(Duration::from_millis(10), move || {
            let mut still_connect = false;
            if let Some(node_ref) = weak_node.upgrade() {
                if let Some(node) = node_ref.borrow().update_native_buffered_picture() {
                    if let Some(ref picture) = *node.imp().picture.borrow() {
                        if let Some(ref area) = *node.imp().drawing_area.borrow() {
                            set_parent(picture as *const Picture, area as *const DrawingArea);
                        }
                    }
                }
                still_connect = node_ref.borrow().imp().still_connect.get();
            }
            Continue(still_connect)
        });
    }

    fn unparent(&self) {
        if let Some(ref picture) = *self.rc().borrow().imp().picture.borrow() {
            picture.unparent();
        }
    }

    fn set_verbose(&self, verbose: bool) {
        self.rc().borrow().imp().is_verbose.set(verbose);
    }

    fn set_hibpi_aware(&self, hibpi_aware: bool) {
        self.rc().borrow().imp().hibpi_aware.set(hibpi_aware);
    }

    fn terminate(&self) {
        if self.rc().borrow().imp().key.get() < 0 {
            return;
        }
        native_terminate_at(self.rc().borrow().imp().key.get());
        self.rc().borrow().imp().still_connect.set(false);
    }
}
