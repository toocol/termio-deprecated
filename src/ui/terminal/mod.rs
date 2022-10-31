mod imp;

use gtk::{glib, traits::WidgetExt, Inhibit};

glib::wrapper! {
    pub struct NativeTerminalEmulator(ObjectSubclass<imp::NativeTerminalEmulator>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl NativeTerminalEmulator {
    pub fn init_key_binding(&self) {
        let controller = gtk::EventControllerKey::new();
        controller.connect_key_pressed(move |_controller, key, keycode, _modfier| {
            println!("name: {:#?}, code: {}", key.name(), keycode);
            Inhibit(false)
        });
        self.add_controller(&controller);
    }
}
