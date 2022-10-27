use std::cell::RefCell;
use std::rc::Rc;
use std::time::Duration;

use gtk::gdk_pixbuf::Pixbuf;
use gtk::glib::clone::Downgrade;
use gtk::glib::timeout_add_local;
use gtk::prelude::*;
use gtk::Image;
use log::info;

#[derive(Default)]
pub struct NativeNode {
    pub image: Rc<RefCell<Image>>,
    pub image_buffer: Option<RefCell<Pixbuf>>,
}

impl NativeNode {
    fn update_native_image(&self) {
        info!("Update native image")
    }
}

pub trait NativeNodeImpl {
    const CONNECTION_NAME: &'static str;

    fn rc(&self) -> Rc<NativeNode>;

    fn connect(&self) {
        let weak_node = self.rc().downgrade();
        timeout_add_local(Duration::from_millis(10), move || {
            if let Some(native_node) = weak_node.upgrade() {
                native_node.update_native_image();
            }
            Continue(true)
        });
    }

    fn image(&self) -> Rc<RefCell<Image>> {
        self.rc().image.clone()
    }
}
