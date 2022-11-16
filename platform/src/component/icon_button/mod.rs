mod imp;

use gtk::glib;
use glib::Object;

glib::wrapper! {
    pub struct IconButton(ObjectSubclass<imp::IconButton>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl IconButton {
    pub fn new() -> Self {
        Object::builder().build()
    }
}