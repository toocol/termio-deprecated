mod imp;

use gtk::glib;

glib::wrapper! {
    pub struct MenuItem(ObjectSubclass<imp::MenuItem>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}