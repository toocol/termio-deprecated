#![allow(dead_code)]
use std::cell::RefCell;
use std::rc::Rc;
use std::slice;
use std::time::Duration;

use crate::native::native_adapter::*;
use gtk::Picture;
use gtk::glib;
use gtk::gdk_pixbuf::Pixbuf;
use gtk::glib::Object;
use gtk::glib::clone::Downgrade;
use gtk::glib::timeout_add_local;
use gtk::glib::Bytes;
use gtk::prelude::*;
use gtk::subclass::prelude::*;
use log::info;
use utilities::TimeStamp;

glib::wrapper! {
    pub struct NativeNodeObject(ObjectSubclass<NativeNode>);
}

impl NativeNodeObject {
    pub fn new() -> Self {
        Object::new(&[])
    }
}

pub struct NativeNode {
    pub picture: Option<Rc<RefCell<Picture>>>,
    pub pixel_buffer: Option<RefCell<Pixbuf>>,
    pub key: i32,
    pub width: i32,
    pub height: i32,

    still_connect: bool,
    is_verbose: bool,
    hibpi_aware: bool,
    button_state: i32,
    locking_error: bool,

    num_values: i32,
    fps_counter: i32,
    fps_values: [f64; 10],
    frame_timestamp: u64,
}

impl Default for NativeNode {
    fn default() -> Self {
        Self {
            picture: None,
            pixel_buffer: None,
            key: -1,
            width: 0,
            height: 0,
            still_connect: false,
            is_verbose: false,
            hibpi_aware: false,
            button_state: 0,
            locking_error: false,
            num_values: 10,
            fps_counter: 0,
            fps_values: [0.0; 10],
            frame_timestamp: 0,
        }
    }
}

#[glib::object_subclass]
impl ObjectSubclass for NativeNode {
    const NAME: &'static str = "NativeNode";

    type Type = NativeNodeObject;
}

impl ObjectImpl for NativeNode {
    fn constructed(&self) {
        self.parent_constructed();
        info!("Construct `NativeNode`")
    }
}

impl NativeNode {
    fn update_native_buffered_picture(&mut self) -> Option<&Self> {
        let mut flag = false;
        let current_timestamp = TimeStamp::timestamp();
        self.locking_error = !native_lock(self.key);
        if self.locking_error {
            return None;
        }

        let dirty = native_is_dirty(self.key);
        let is_ready = native_is_buffer_ready(self.key);

        native_process_native_events(self.key);

        if !dirty || !is_ready {
            native_unlock(self.key);
            return None;
        }

        let current_w = native_get_w(self.key);
        let current_h = native_get_h(self.key);

        let picture_w = match &self.picture {
            Some(picture) => picture.borrow().width(),
            None => 0,
        };
        let picture_h = match &self.picture {
            Some(picture) => picture.borrow().height(),
            None => 0,
        };

        if &None == &self.picture || picture_w != current_w || picture_h != current_h {
            if self.is_verbose {
                println!(
                    "[{}]> -> new img instance, resize W: {}, H: {}",
                    self.key, current_w, current_h
                );
            }
            // When resize, unparent the old picture.
            if let Some(picture) = &self.picture {
                println!("[{}] -> Unparent the old picture.", self.key);
                picture.borrow().unparent();
            }

            unsafe {
                let buffer = slice::from_raw_parts(
                    native_get_buffer(self.key),
                    (current_w * current_h * 4) as usize,
                );
                let pixbuf = Pixbuf::from_bytes(
                    &Bytes::from_static(buffer),
                    gtk::gdk_pixbuf::Colorspace::Rgb,
                    true,
                    8,
                    current_w,
                    current_h,
                    current_w * 4,
                );

                let picture = Picture::for_pixbuf(&pixbuf);
                picture.set_can_shrink(false);
                picture.set_halign(gtk::Align::Start);
                picture.set_valign(gtk::Align::Start);
                self.picture
                    .replace(Rc::new(RefCell::new(picture)));
                self.pixel_buffer.replace(RefCell::new(pixbuf));
                flag = true;
            }
        } // Process if picture is None, or window size was changed.

        // if let Some(image) = &self.picture {
        //     if let Some(buffer) = &self.image_buffer {
        //         info!("Image size, w:{}, h{}", image.borrow().width(), image.borrow().height());
        //         image.borrow_mut().set_from_pixbuf(Some(&buffer.borrow()));
        //     }
        // }

        // Have update the image, not dirty anymore
        native_set_dirty(self.key, false);
        let width = self.width;
        let height = self.height;
        let scale_factor = 1.0;
        if (width as f64 != native_get_w(self.key) as f64 / scale_factor
            || height as f64 != native_get_h(self.key) as f64 / scale_factor)
            && width > 0
            && height > 0
        {
            if self.is_verbose {
                println!(
                    "[{}]> requesting buffer resize W: {}, H: {}",
                    self.key, width, height
                );
            }
            native_resize(
                self.key,
                width * scale_factor as i32,
                height * scale_factor as i32,
            );
        }
        native_unlock(self.key);

        if self.is_verbose {
            let duration = current_timestamp - self.frame_timestamp;
            let fps = (1e9 as f64) / (duration as f64);
            self.fps_values[self.fps_counter as usize] = fps;
            if self.fps_counter == self.num_values - 1 {
                let mut fps_average = 0.0;
                for fps_val in self.fps_values.iter() {
                    fps_average += fps_val;
                }
                fps_average /= self.num_values as f64;
                self.fps_counter = 0;
                println!("[{}]> fps: {}", self.key, fps_average);
            }
            self.fps_counter += 1;
            self.frame_timestamp = current_timestamp;
        }

        if flag {
            Some(self)
        } else {
            None
        }
    }
}

pub trait NativeNodeImpl {
    const CONNECTION_NAME: &'static str;

    fn rc(&self) -> Rc<RefCell<NativeNode>>;

    fn connect<T>(&self, set_parent: T)
    where
        T: Fn(*const Picture) + 'static,
    {
        let node_rc = self.rc();
        let weak_node = node_rc.downgrade();

        if node_rc.borrow().key < 0 || !native_is_connected(node_rc.borrow().key) {
            node_rc.borrow_mut().key = native_connect_to(Self::CONNECTION_NAME);
        }
        node_rc.borrow_mut().still_connect = true;

        timeout_add_local(Duration::from_millis(10), move || {
            let mut still_connect = false;
            if let Some(native_node) = weak_node.upgrade() {
                if let Some(node) = native_node.borrow_mut().update_native_buffered_picture() {
                    info!("First create native image picture.");
                    if let Some(picture) = &node.picture {
                        set_parent(&*picture.borrow() as *const Picture);
                    }
                }
                still_connect = native_node.borrow().still_connect;
            }
            Continue(still_connect)
        });
    }

    fn unparent(&self) {
        if let Some(picture) = &self.rc().borrow().picture {
            picture.borrow().unparent();
        }
    }

    fn set_verbose(&self, verbose: bool) {
        self.rc().borrow_mut().is_verbose = verbose;
    }

    fn set_hibpi_aware(&self, hibpi_aware: bool) {
        self.rc().borrow_mut().hibpi_aware = hibpi_aware;
    }

    fn terminate(&self) {
        let node_rc = self.rc();
        if node_rc.borrow_mut().key < 0 {
            return;
        }
        native_terminate_at(node_rc.borrow().key);
        node_rc.borrow_mut().still_connect = false;
    }
}
