mod imp;

use gtk::glib::{self, Object};

glib::wrapper! {
    pub struct SettingPanel(ObjectSubclass<imp::SettingPanel>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl SettingPanel {
    pub fn new() -> Self {
        Object::new(&[])
    }
}