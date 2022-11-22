use std::{cell::RefCell, rc::Rc};

use gtk::{glib, subclass::prelude::*, traits::WidgetExt};
use crate::native_node::{NativeNodeImpl, NativeNodeObject};

pub struct NativeTerminalEmulator {
    pub native_node_object: Rc<RefCell<NativeNodeObject>>,
}

impl NativeTerminalEmulator {}

impl Default for NativeTerminalEmulator {
    fn default() -> Self {
        Self {
            native_node_object: Rc::new(RefCell::new(NativeNodeObject::new())),
        }
    }
}

#[glib::object_subclass]
impl ObjectSubclass for NativeTerminalEmulator {
    const NAME: &'static str = "NativeTerminalEmulator";

    type Type = super::NativeTerminalEmulator;

    type ParentType = gtk::Widget;

    fn class_init(klass: &mut Self::Class) {
        klass.set_layout_manager_type::<gtk::BinLayout>();
    }
}

impl ObjectImpl for NativeTerminalEmulator {
    fn constructed(&self) {
        self.parent_constructed();
        self.instance().setup_callbakcs();

        self.native_node_object.borrow().set_verbose(true);
        self.native_node_object.borrow().set_hibpi_aware(true);
        self.native_node_object
            .borrow()
            .imp()
            .drawing_area
            .borrow()
            .set_parent(self.instance().as_ref());
        self.connect();
    }

    fn dispose(&self) {
        self.native_node_object.borrow().unparent();
    }
}

impl WidgetImpl for NativeTerminalEmulator {}

impl NativeNodeImpl for NativeTerminalEmulator {
    const CONNECTION_NAME: &'static str = "_emulator_mem";

    fn rc(&self) -> Rc<RefCell<NativeNodeObject>> {
        self.native_node_object.clone()
    }
}
