mod imp;

use gtk::{
    glib::{self, Object},
    Label, prelude::ToValue,
};

glib::wrapper! {
    pub struct MenuSection(ObjectSubclass<imp::MenuSection>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl MenuSection {
    pub fn new(label: Option<Label>) -> Self {
        let mut properties: Vec<(&'static str, &dyn ToValue)> = vec![];
        if let Some(ref label) = label {
            properties.push(("label", label));
        }
        Object::new(&properties)
    }
}
