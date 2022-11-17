mod imp;

use glib::Object;
use gtk::{glib, prelude::IsA, prelude::*, subclass::prelude::*, Widget};

glib::wrapper! {
    pub struct SvgIcon(ObjectSubclass<imp::SvgIcon>);
}

impl SvgIcon {
    pub fn new(svg_path: &str) -> Self {
        let icon: SvgIcon = Object::builder().property("svg", svg_path).build();
        icon.imp().initialize_image();
        icon
    }

    pub fn set_parent<T: IsA<Widget>>(&self, parent: &T) {
        self.imp()
            .image
            .borrow()
            .as_ref()
            .expect("`image` of FontAwesomeIcon is None.")
            .set_parent(parent)
    }

    pub fn unparent(&self) {
        self.imp()
            .image
            .borrow()
            .as_ref()
            .expect("`image` of FontAwesomeIcon is None.")
            .unparent()
    }
}
