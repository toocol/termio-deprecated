mod imp;

use gtk::glib;

glib::wrapper! {
    pub struct MenuModel(ObjectSubclass<imp::MenuModel>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}