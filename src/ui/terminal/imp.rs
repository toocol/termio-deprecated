use std::{rc::Rc, cell::RefCell};

use log::info;
use gtk::prelude::Cast;
use gtk::subclass::prelude::*;
use gtk::traits::WidgetExt;
use gtk::glib;
use platform::native_node::{NativeNodeImpl, NativeNode};

#[derive(Default)]
pub struct NativeTerminalEmulator {
    node: Rc<RefCell<NativeNode>>
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
        self.connect();
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