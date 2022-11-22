mod imp;

use glib::Object;
use gtk::glib;

glib::wrapper! {
    pub struct EditionMark(ObjectSubclass<imp::EditionMark>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl EditionMark {
    pub fn new(code: &str, label: &str) -> Self {
        Object::builder()
            .property("code", code)
            .property("label", label)
            .build()
    }
}