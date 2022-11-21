mod imp;

use glib::Object;
use gtk::glib;
use serde::{Deserialize, Serialize};

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

#[derive(Serialize, Deserialize)]
pub struct EditionMarkJsonObject {
    pub code: String,
    pub label: String,
}

impl EditionMarkJsonObject {
    pub fn to_bottom_status_bar_item(&self) -> EditionMark {
        EditionMark::new(&self.code, &self.label)
    }
}
