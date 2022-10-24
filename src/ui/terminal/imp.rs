use std::cell::RefCell;
use std::rc::Rc;

use log::info;
use gtk::gdk_pixbuf::Pixbuf;
use gtk::prelude::Cast;
use gtk::subclass::prelude::*;
use gtk::traits::WidgetExt;
use gtk::{glib, Image};

#[derive(Default)]
pub struct NativeTerminalEmulator {
    pub image_buffer: Option<RefCell<Pixbuf>>,
    pub image: Rc<RefCell<Image>>,
}

#[glib::object_subclass]
impl ObjectSubclass for NativeTerminalEmulator {
    const NAME: &'static str = "NativeTerminalEmulator";

    type Type = super::NativeTerminalEmulator;

    type ParentType = gtk::Widget;

    fn class_init(klass: &mut Self::Class) {
        klass.set_layout_manager_type::<gtk::BoxLayout>();
    }
}

impl ObjectImpl for NativeTerminalEmulator {
    fn constructed(&self) {
        self.parent_constructed();

        let _layout = self
            .instance()
            .layout_manager()
            .unwrap()
            .downcast::<gtk::BoxLayout>()
            .unwrap();
        self.image.borrow().set_parent(&self.instance().to_owned());
        info!("NativeTerminalEmulator constructed.")
    }
}

impl WidgetImpl for NativeTerminalEmulator {}
