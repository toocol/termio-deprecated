mod imp;

use gtk::glib::{self, Object};

glib::wrapper! {
    pub struct CommandPanel(ObjectSubclass<imp::CommandPanel>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl CommandPanel {
    pub fn new() -> Self {
        Object::new(&[])
    }
}