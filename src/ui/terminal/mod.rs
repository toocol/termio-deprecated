mod imp;

use gtk::glib;

glib::wrapper! {
    pub struct NativeTerminalEmulator(ObjectSubclass<imp::NativeTerminalEmulator>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}