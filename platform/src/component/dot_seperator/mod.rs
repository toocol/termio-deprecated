mod imp;

pub use gtk::glib;
use gtk::glib::Object;

glib::wrapper! {
    pub struct DotSeperator(ObjectSubclass<imp::DotSeperator>)
        @extends gtk::DrawingArea, gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl DotSeperator {
    pub fn new() -> Self {
        Object::new(&[])
    }
}
