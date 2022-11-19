mod imp;

use glib::Object;
use gtk::{glib, prelude::IsA, prelude::*, subclass::prelude::*, Widget};

glib::wrapper! {
    pub struct SvgIcon(ObjectSubclass<imp::SvgIcon>);
}

impl SvgIcon {
    pub fn new(icon_name: &str) -> Self {
        Object::builder().property("icon-name", icon_name).build()
    }

    pub fn set_parent<T: IsA<Widget>>(&self, parent: &T) {
        self.imp()
            .image
            .borrow()
            .as_ref()
            .expect("`image` of SvgIcon is None.")
            .set_parent(parent)
    }

    pub fn unparent(&self) {
        self.imp()
            .image
            .borrow()
            .as_ref()
            .expect("`image` of SvgIcon is None.")
            .unparent()
    }
}
