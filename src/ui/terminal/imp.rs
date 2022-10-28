use std::{cell::RefCell, rc::Rc};

use glib::clone;
use gtk::glib;
use gtk::prelude::Cast;
use gtk::subclass::prelude::*;
use gtk::traits::WidgetExt;
use log::info;
use platform::native_node::{NativeNode, NativeNodeImpl};

#[derive(Default)]
pub struct NativeTerminalEmulator {
    node: Rc<RefCell<NativeNode>>,
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
        // self.image().borrow().set_parent(&self.instance().to_owned());
        self.set_verbose(true);
        self.set_hibpi_aware(true);
        self.connect(clone!(@weak self as widget => move || {
            widget.image().take().unwrap().borrow_mut().set_parent(&widget.instance().to_owned());
        }));
        info!("NativeTerminalEmulator constructed.")
    }

    fn dispose(&self) {
        if let Some(image) = self.image() {
            image.borrow().unparent();
        }
    }
}

impl WidgetImpl for NativeTerminalEmulator {}

impl NativeNodeImpl for NativeTerminalEmulator {
    const CONNECTION_NAME: &'static str = "_emulator_mem";

    fn rc(&self) -> Rc<RefCell<NativeNode>> {
        self.node.clone()
    }
}
