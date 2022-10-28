#![allow(dead_code)]
use std::cell::RefCell;
use std::rc::Rc;
use std::slice;
use std::time::Duration;

use crate::native::native_adapter::*;
use gtk::gdk_pixbuf::Pixbuf;
use gtk::glib::clone::Downgrade;
use gtk::glib::timeout_add_local;
use gtk::glib::Bytes;
use gtk::prelude::*;
use gtk::Image;
use utilities::TimeStamp;

pub struct NativeNode {
    pub image: Option<Rc<RefCell<Image>>>,
    pub image_buffer: Option<RefCell<Pixbuf>>,
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
    fps_values: Vec<f64>,
    frame_timestamp: u64,
}

impl Default for NativeNode {
    fn default() -> Self {
        Self {
            image: None,
            image_buffer: None,
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
            fps_values: vec![],
            frame_timestamp: 0,
        }
    }
}

impl NativeNode {
    fn update_native_image(&mut self) -> Option<()> {
        let mut flag = false;
        let _current_timestamp = TimeStamp::timestamp();
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

        let current_w: i32 = native_get_w(self.key);
        let current_h: i32 = native_get_h(self.key);

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
            // When resize, unparent the old image.
            if let Some(image) = image_opt {
                image.borrow().unparent();
            }

            unsafe {
                let buffer = slice::from_raw_parts(
                    native_get_buffer(self.key),
                    (current_w * current_h) as usize,
                );
                let pixbuf = Pixbuf::from_bytes(
                    &Bytes::from_static(buffer),
                    gtk::gdk_pixbuf::Colorspace::Rgb,
                    false,
                    8,
                    current_w,
                    current_h,
                    current_w * 4,
                );
                self.image_buffer.replace(RefCell::new(pixbuf));
                self.image
                    .replace(Rc::new(RefCell::new(Image::from_pixbuf(Some(
                        &self.image_buffer.take().unwrap().borrow(),
                    )))));
                flag = true;
            }
        } // Process if image is None

        self.image
            .take()
            .unwrap()
            .borrow_mut()
            .set_from_pixbuf(Some(&self.image_buffer.take().unwrap().borrow()));

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
                println!("[{}]> requesting buffer resize W: {}, H: {}", self.key, width, height);
            }
            native_resize(self.key, width * scale_factor as i32, height * scale_factor as i32);
        }
        native_unlock(self.key);

        if flag {
            Some(())
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
        T: Fn() + 'static,
    {
        let node_rc = self.rc();
        let weak_node = node_rc.downgrade();

        node_rc.borrow_mut().still_connect = true;

        timeout_add_local(Duration::from_millis(10), move || {
            let mut still_connect = false;
            if let Some(native_node) = weak_node.upgrade() {
                if let Some(_) = native_node.borrow_mut().update_native_image() {
                    set_parent();
                }
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
