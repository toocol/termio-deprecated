mod imp;

use gtk::glib::{self, Object};

glib::wrapper! {
    pub struct CompositeLabel(ObjectSubclass<imp::CompositeLabel>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl CompositeLabel {
    pub fn new() -> Self {
        Object::new(&[])
    }
}