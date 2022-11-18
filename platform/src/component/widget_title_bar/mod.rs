mod imp;

use glib::Object;
use gtk::glib;

glib::wrapper! {
    pub struct WidgetTitleBar(ObjectSubclass<imp::WidgetTitleBar>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl WidgetTitleBar {
    pub fn new() -> Self {
        Object::builder().build()
    }
}
