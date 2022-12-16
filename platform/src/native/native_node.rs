#![allow(dead_code)]
use std::{
    cell::{Cell, RefCell},
    rc::Rc,
    sync::{
        atomic::{AtomicI32, Ordering},
        mpsc::{channel, Sender},
    },
    thread,
    time::Duration,
};

use crate::{
    cross_process_event_dispatch, key_code_mapping::QtCodeMapping, native::native_adapter::*,
    CrossProcessEvent, GtkMouseButton, QtMouseButton,
};
use gtk::{
    cairo::{ffi::cairo_surface_destroy, ImageSurface},
    gdk::{Key, ModifierType},
    glib::{self, clone, clone::Downgrade, timeout_add_local, Object},
    prelude::*,
    subclass::prelude::*,
    Align,
};
use log::{debug, error};
use utilities::TimeStamp;

const PRIMARY_BUFFER: i32 = 1;
const SECONDARY_BUFFER: i32 = -1;

glib::wrapper! {
    pub struct NativeNodeObject(ObjectSubclass<NativeNode>)
        @extends gtk::DrawingArea, gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

static MODIFIER: AtomicI32 = AtomicI32::new(0);

pub struct NativeNode {
    pub event_sender: RefCell<Option<Sender<CrossProcessEvent>>>,
    pub primary_buffer: RefCell<Option<*mut u8>>,
    pub secondary_buffer: RefCell<Option<*mut u8>>,
    pub key: Cell<i32>,
    pub width: Cell<i32>,
    pub height: Cell<i32>,
    pub image_width: Cell<i32>,
    pub image_height: Cell<i32>,

    self_triggered: Cell<bool>,
    still_connect: Cell<bool>,
    is_verbose: Cell<bool>,
    hibpi_aware: Cell<bool>,
    button_state: Cell<i32>,

    fps_counter: Cell<i32>,
    frame_timestamp: Cell<u64>,
    num_values: i32,
    fps_values: RefCell<[f64; 10]>,
}

impl Default for NativeNode {
    fn default() -> Self {
        Self {
            event_sender: RefCell::new(None),
            primary_buffer: RefCell::new(None),
            secondary_buffer: RefCell::new(None),
            key: Cell::new(-1),
            width: Cell::new(0),
            height: Cell::new(0),
            image_width: Cell::new(0),
            image_height: Cell::new(0),
            self_triggered: Cell::new(false),
            still_connect: Cell::new(false),
            is_verbose: Cell::new(false),
            hibpi_aware: Cell::new(false),
            button_state: Cell::new(0),
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

    pub fn dispatch(&self, evt: CrossProcessEvent) {
        if let Err(e) = self
            .imp()
            .event_sender
            .borrow()
            .as_ref()
            .expect("`event_sender` should initialize firsr.")
            .send(evt)
        {
            error!("Send CrossProcessEvent failed, e={:#?}", e);
        }
    }

    pub fn create_ssh_session(
        &self,
        session_id: u64,
        host: &str,
        user: &str,
        password: &str,
        timestmap: u64,
    ) {
        let evt = CrossProcessEvent::new_create_ssh_session_event(
            self.imp().key.get(),
            session_id,
            host,
            user,
            password,
            timestmap,
        );
        self.dispatch(evt);
    }

    pub fn shell_startup(&self, session_id: u64, param: &str, timestamp: u64) {
        let evt = CrossProcessEvent::new_shell_startup_event(
            self.imp().key.get(),
            session_id,
            param,
            timestamp,
        );
        self.dispatch(evt);
    }

    pub fn request_focus(&self, is_focus: bool) {
        let evt = CrossProcessEvent::new_request_focus_event(self.imp().key.get(), is_focus);
        self.dispatch(evt);
    }

    pub fn react_key_pressed_event(&self, key: Key, keycode: u32, modifier: ModifierType) {
        debug!(
            "`NativeNode` key pressed -> key: {:?}, name: {:?}, code: {}, modifier: {:?}, qt_code: {}",
            key,
            key.name(),
            keycode,
            modifier,
            QtCodeMapping::get_qt_code(keycode)
        );
        MODIFIER.store(QtCodeMapping::get_qt_modifier(modifier), Ordering::SeqCst);
        let character = match key.to_unicode() {
            Some(c) => c.to_string(),
            None => "".to_string(),
        };
        let evt = CrossProcessEvent::new_key_pressed_event(
            self.imp().key.get(),
            character,
            QtCodeMapping::get_qt_code(keycode),
            MODIFIER.load(Ordering::SeqCst),
        );
        self.dispatch(evt);
    }

    pub fn react_key_released_event(&self, key: Key, keycode: u32, modifier: ModifierType) {
        debug!(
            "`NativeNode` key released -> name: {:?}, code: {}, modifier: {:?}, qt_code: {}",
            key.name(),
            keycode,
            modifier,
            QtCodeMapping::get_qt_code(keycode)
        );
        MODIFIER.store(0, Ordering::SeqCst);
        let character = match key.to_unicode() {
            Some(c) => c.to_string(),
            None => "".to_string(),
        };
        let evt = CrossProcessEvent::new_key_released_event(
            self.imp().key.get(),
            character,
            QtCodeMapping::get_qt_code(keycode),
            MODIFIER.load(Ordering::SeqCst),
        );
        self.dispatch(evt);
    }

    pub fn react_mouse_pressed_event(&self, n_press: i32, x: f64, y: f64, button: GtkMouseButton) {
        debug!(
            "`NativeNode` mouse pressed -> n_press: {}, x: {}, y: {}",
            n_press, x, y
        );
        let evt = CrossProcessEvent::new_mouse_pressed_event(
            self.imp().key.get(),
            QtMouseButton::from_gtk_button(button),
            x,
            y,
            MODIFIER.load(Ordering::SeqCst),
        );
        self.dispatch(evt);
    }

    pub fn react_mouse_released_event(&self, n_press: i32, x: f64, y: f64, button: GtkMouseButton) {
        debug!(
            "`NativeNode` mouse released -> n_press: {}, x: {}, y: {}",
            n_press, x, y
        );
        let evt = CrossProcessEvent::new_mouse_released_event(
            self.imp().key.get(),
            QtMouseButton::from_gtk_button(button),
            x,
            y,
            MODIFIER.load(Ordering::SeqCst),
        );
        self.dispatch(evt);
    }

    pub fn react_mouse_motion_enter(&self, x: f64, y: f64) {
        debug!("`NativeNode` motion enter, {} {}", x, y);
        let evt = CrossProcessEvent::new_mouse_enter_event(
            self.imp().key.get(),
            x,
            y,
            MODIFIER.load(Ordering::SeqCst),
        );
        self.dispatch(evt);
    }

    pub fn react_mouse_motion_leave(&self) {
        debug!("`NativeNode` motion leave");
        let evt = CrossProcessEvent::new_mouse_leave_event(
            self.imp().key.get(),
            MODIFIER.load(Ordering::SeqCst),
        );
        self.dispatch(evt);
    }

    pub fn react_mouse_motion_move(&self, x: f64, y: f64) {
        let evt = CrossProcessEvent::new_mouse_move_event(
            self.imp().key.get(),
            x,
            y,
            MODIFIER.load(Ordering::SeqCst),
        );
        self.dispatch(evt);
    }

    pub fn react_mouse_wheel(&self, x: f64, y: f64) {
        debug!("`NativeNode` mouse wheel: x: {}, y: {}", x, y);
        let evt = CrossProcessEvent::new_mouse_wheel_event(
            self.imp().key.get(),
            0.,
            0.,
            -y * 120.,
            MODIFIER.load(Ordering::SeqCst),
        );
        self.dispatch(evt);
    }

    fn rendering_native_buffer(&self) {
        let imp = self.imp();
        let current_timestamp = TimeStamp::timestamp();
        let key = imp.key.get();

        while native_has_native_events(key) {
            let mut evt = native_get_native_event(key);
            native_drop_native_event(key);

            let params;

            self.activate_action(
                &evt.action_name,
                if evt.params.is_none() {
                    None
                } else {
                    params = evt.params.take().unwrap().to_variant();
                    Some(&params)
                },
            )
            .expect(format!("Activate action `{}` failed", evt.action_name).as_str());
        }

        if !native_lock(key) {
            error!("[{}] -> locking error.", key);
            return;
        }

        let dirty = native_is_dirty(key);
        let is_ready = native_is_buffer_ready(key);

        if !dirty || !is_ready {
            native_unlock(key);
            return;
        }

        let current_w = native_get_w(key);
        let current_h = native_get_h(key);

        let image_w = imp.image_width.get();
        let image_h = imp.image_height.get();

        if image_w != current_w || image_h != current_h {
            if imp.is_verbose.get() {
                debug!(
                    "[{}]> -> new native buffer, resize W: {}, H: {}",
                    key, current_w, current_h
                );
            }
            imp.image_width.set(current_w);
            imp.image_height.set(current_h);

            self.set_content_width(current_w);
            self.set_content_height(current_h);
        }

        self.imp().self_triggered.set(true);
        self.queue_draw();

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
                debug!("[{}] -> fps: {}", key, fps_average);
            }
            imp.fps_counter.set(imp.fps_counter.get() + 1);
            imp.frame_timestamp.set(current_timestamp);
        }
    }

    pub fn resize(&self, width: i32, height: i32) {
        let old_w = self.imp().width.get();
        let old_h = self.imp().height.get();
        if old_w != width || old_h != height {
            self.imp().width.set(width);
            self.imp().height.set(height);
            let key = self.imp().key.get();
            if native_lock(key) {
                native_resize(key, width, height);
                native_unlock(key);
            }
        }
    }

    pub fn terminate(&self) {
        if self.imp().key.get() < 0 {
            return;
        }
        native_terminate_at(self.imp().key.get());
        self.imp().still_connect.set(false);
    }

    pub fn set_verbose(&self, verbose: bool) {
        self.imp().is_verbose.set(verbose);
    }

    pub fn set_hibpi_aware(&self, hibpi_aware: bool) {
        self.imp().hibpi_aware.set(hibpi_aware);
    }
}

#[glib::object_subclass]
impl ObjectSubclass for NativeNode {
    const NAME: &'static str = "NativeNode";

    type Type = NativeNodeObject;

    type ParentType = gtk::DrawingArea;
}

impl ObjectImpl for NativeNode {
    fn constructed(&self) {
        self.parent_constructed();
        let obj = self.instance();

        obj.set_focusable(true);
        obj.set_halign(Align::Start);
        obj.set_valign(Align::Start);

        let (sender, receiver) = channel::<CrossProcessEvent>();
        self.event_sender.borrow_mut().replace(sender);
        thread::spawn(move || {
            while let Ok(evt) = receiver.recv() {
                cross_process_event_dispatch(evt);
            }
            debug!("`CrossProcessEvent` receive finished.")
        });
    }
}

pub trait NativeNodeImpl {
    const CONNECTION_NAME: &'static str;

    fn rc(&self) -> Rc<RefCell<NativeNodeObject>>;

    fn connect(&self) {
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

        let node = node_rc.borrow();
        let imp = node.imp();
        let key = imp.key.get();

        unsafe {
            imp.primary_buffer
                .borrow_mut()
                .replace(native_get_primary_buffer(key));
            imp.secondary_buffer
                .borrow_mut()
                .replace(native_get_secondary_buffer(key));

            node_rc.borrow().set_draw_func(clone!(@weak imp as node => move |_drawing_area, cr, _, _| {
                if native_lock_buffer(key) {
                    if node.self_triggered.get() && (!native_is_dirty(key) || !native_is_buffer_ready(key)) {
                        return;
                    }
                    match native_buffer_status(key) {
                        PRIMARY_BUFFER => {
                            let primary_surface = ImageSurface::create_for_data_unsafe(
                                node.primary_buffer.borrow().as_ref().expect("`primary_buffer` was None.").clone(),
                                gtk::cairo::Format::ARgb32,
                                node.image_width.get(),
                                node.image_height.get(),
                                node.image_width.get() * 4,
                            )
                            .expect("Create `ImageSurface` failed.");

                            cr.set_source_surface(&primary_surface, 0., 0.)
                                .expect("Context set source surface failed.");
                            cr.paint().expect("Invalid pixbuf.");
                            cr.set_source_rgba(0., 0., 0., 0.);
                            cairo_surface_destroy(primary_surface.to_raw_none());
                        }
                        SECONDARY_BUFFER => {
                            let secodnary_surface = ImageSurface::create_for_data_unsafe(
                                node.secondary_buffer.borrow().as_ref().expect("`primary_buffer` was None.").clone(),
                                gtk::cairo::Format::ARgb32,
                                node.image_width.get(),
                                node.image_height.get(),
                                node.image_width.get() * 4,
                            )
                            .expect("Create `ImageSurface` failed.");

                            cr.set_source_surface(&secodnary_surface, 0., 0.)
                                .expect("Context set source surface failed.");
                            cr.paint().expect("Invalid pixbuf.");
                            cr.set_source_rgba(0., 0., 0., 0.);
                            cairo_surface_destroy(secodnary_surface.to_raw_none());
                        }
                        _ => unimplemented!(),
                    }
                    // Have update the image, not dirty anymore, toggle the buffer status
                    node.self_triggered.set(false);
                    native_set_dirty(key, false);
                    native_unlock_buffer(key);
                    native_toggle_buffer(key);
                }
            }));
        }

        timeout_add_local(Duration::from_millis(1), move || {
            let mut still_connect = false;
            if let Some(node_ref) = weak_node.upgrade() {
                node_ref.borrow().rendering_native_buffer();
                still_connect = node_ref.borrow().imp().still_connect.get();
            }
            Continue(still_connect)
        });
    }
}

impl WidgetImpl for NativeNode {}

impl DrawingAreaImpl for NativeNode {}
