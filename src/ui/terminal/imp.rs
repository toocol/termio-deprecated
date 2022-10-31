use std::{cell::RefCell, rc::Rc};

use glib::clone;
use gtk::prelude::Cast;
use gtk::subclass::prelude::*;
use gtk::traits::WidgetExt;
use gtk::{glib, Picture};
use log::info;
use platform::native_node::{NativeNodeImpl, NativeNodeObject};

pub struct NativeTerminalEmulator {
    pub native_node_object: Rc<RefCell<NativeNodeObject>>,
}

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
        // klass.set_layout_manager_type::<gtk::BoxLayout>();
        klass.set_layout_manager_type::<gtk::BinLayout>();
    }
}

impl ObjectImpl for NativeTerminalEmulator {
    fn constructed(&self) {
        self.parent_constructed();

        let _layout = self
            .instance()
            .layout_manager()
            .unwrap()
            .downcast::<gtk::BinLayout>()
            .unwrap();

        self.set_verbose(true);
        self.set_hibpi_aware(true);
        self.connect(clone!(@weak self as widget => move |picture| {
            unsafe {
                let picture: &Picture = <Picture>::as_ref(&*picture);
                picture.set_parent(&widget.instance().to_owned());
                info!("Bind native buffered picture to NativeTerminalEmulator.");
            }
        }));
        info!("NativeTerminalEmulator constructed.")
    }

    fn dispose(&self) {
        self.unparent();
    }
}

impl WidgetImpl for NativeTerminalEmulator {
    fn realize(&self) {
        self.parent_realize();
        let allocation = self.instance().allocation();
        println!(
            "Realize! w:{}, h:{}",
            allocation.width(),
            allocation.height()
        );
    }

    fn show(&self) {
        self.parent_show();
        let allocation = self.instance().allocation();
        println!("Show! w:{}, h:{}", allocation.width(), allocation.height());
    }

    fn request_mode(&self) -> gtk::SizeRequestMode {
        gtk::SizeRequestMode::HeightForWidth
    }

    fn measure(&self, orientation: gtk::Orientation, _for_size: i32) -> (i32, i32, i32, i32) {
        if orientation == gtk::Orientation::Vertical {
            (50, 50, -1, -1)
        } else {
            (50, 50, -1, -1)
        }
    }

    fn snapshot(&self, snapshot: &gtk::Snapshot) {
        self.parent_snapshot(snapshot);
        self.native_node_object.borrow().process_snapshot();
    }
}

impl NativeNodeImpl for NativeTerminalEmulator {
    const CONNECTION_NAME: &'static str = "_emulator_mem";

    fn rc(&self) -> Rc<RefCell<NativeNodeObject>> {
        self.native_node_object.clone()
    }
}
