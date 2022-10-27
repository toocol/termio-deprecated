#![allow(dead_code)]
use std::cell::RefCell;
use std::rc::Rc;
use std::time::Duration;

use crate::native::native_adapter::*;
use gtk::gdk_pixbuf::Pixbuf;
use gtk::glib::clone::Downgrade;
use gtk::glib::timeout_add_local;
use gtk::prelude::*;
use gtk::Image;
use utilities::TimeStamp;

pub struct NativeNode {
    pub image: Option<Rc<RefCell<Image>>>,
    pub image_buffer: Option<RefCell<Pixbuf>>,
    pub key: i32,

    still_connect: bool,
    is_verbose: bool,
    button_state: i32,
    locking_error: bool,

    num_values: i32,
    fps_counter: i32,
    fps_values: Vec<f64>,
    frame_timestamp: i64,
}

impl Default for NativeNode {
    fn default() -> Self {
        Self {
            image: None,
            image_buffer: None,
            key: -1,
            still_connect: false,
            is_verbose: false,
            button_state: 0,
            locking_error: false,
            num_values: 10,
            fps_counter: 0,
            fps_values: vec![],
            frame_timestamp: 0,
        }
    }
}

impl NativeNode {
    fn update_native_image(&mut self) {
        let _current_timestamp = TimeStamp::timestamp() as i64;
        self.locking_error = !native_lock(self.key);
        if self.locking_error {
            return;
        }

        let dirty = native_is_dirty(self.key);
        let is_ready = native_is_buffer_ready(self.key);

        native_process_native_events(self.key);

        if !dirty || !is_ready {
            native_unlock(self.key);
            return;
        }

        let current_w = native_get_w(self.key);
        let current_h = native_get_h(self.key);

        let image_opt = self.image.take();
        if None == image_opt
            || self.image.take().unwrap().borrow().width() != current_w
            || self.image.take().unwrap().borrow().height() != current_h
        {
            if self.is_verbose {
                println!(
                    "[{}]> -> new img instance, resize W: {}, H: {}",
                    self.key, current_w, current_h
                );
            }
        }
    }
}

pub trait NativeNodeImpl {
    const CONNECTION_NAME: &'static str;

    fn rc(&self) -> Rc<RefCell<NativeNode>>;

    fn connect(&self) {
        let node_rc = self.rc();
        let weak_node = node_rc.downgrade();

        node_rc.borrow_mut().still_connect = true;

        timeout_add_local(Duration::from_millis(10), move || {
            let mut still_connect = false;
            if let Some(native_node) = weak_node.upgrade() {
                native_node.borrow_mut().update_native_image();
                still_connect = native_node.borrow().still_connect;
            }
            Continue(still_connect)
        });
    }

    fn image(&self) -> Option<Rc<RefCell<Image>>> {
        self.rc().borrow().image.clone()
    }

    fn set_verbose(&self, verbose: bool) {
        self.rc().borrow_mut().is_verbose = verbose;
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
