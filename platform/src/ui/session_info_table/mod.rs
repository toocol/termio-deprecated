mod imp;

use gtk::glib::{self, Object};

glib::wrapper! {
    pub struct SessionInfoTable(ObjectSubclass<imp::SessionInfoTable>)
        @extends gtk::TreeView, gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget, gtk::Scrollable;
}

impl SessionInfoTable {
    pub fn new() -> Self {
        Object::new(&[])
    }
}