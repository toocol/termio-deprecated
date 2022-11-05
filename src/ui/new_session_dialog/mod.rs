mod imp;

use glib::Object;
use gtk::{glib, prelude::IsA, Window};

glib::wrapper! {
    pub struct NewSessionDialog(ObjectSubclass<imp::NewSessionDialog>);
}

impl NewSessionDialog {
    pub fn new<T: IsA<Window>>(_parent: Option<&T>) -> Self {
        Object::new(&[])
    }
}
