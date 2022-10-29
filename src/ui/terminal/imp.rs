use std::{cell::RefCell, rc::Rc};

use glib::clone;
use gtk::{glib, Picture};
use gtk::prelude::Cast;
use gtk::subclass::prelude::*;
use gtk::traits::WidgetExt;
use log::info;
use platform::native_node::{NativeNode, NativeNodeImpl};

#[derive(Default)]
pub struct NativeTerminalEmulator {
    native_node: Rc<RefCell<NativeNode>>,
}

// impl Default for NativeTerminalEmulator {
//     fn default() -> Self {
//         Self { native_node_object: Rc::new(RefCell::new(NativeNodeObject::new())) }
//     }
// }

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

        self.set_verbose(true);
        self.set_hibpi_aware(true);
        self.connect(clone!(@weak self as widget => move |image| {
            unsafe {
                let image: &Picture = <Picture>::as_ref(&*image);
                image.set_parent(&widget.instance().to_owned());
                info!("Bind native buffered picture to NativeTerminalEmulator");
            }
        }));
        info!("NativeTerminalEmulator constructed.")
    }

    fn dispose(&self) {
        self.unparent();
    }
}

impl WidgetImpl for NativeTerminalEmulator {}

impl NativeNodeImpl for NativeTerminalEmulator {
    const CONNECTION_NAME: &'static str = "_emulator_mem";

    fn rc(&self) -> Rc<RefCell<NativeNode>> {
        self.native_node.clone()
    }
}
