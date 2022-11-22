mod imp;

use gtk::glib;

glib::wrapper! {
    pub struct ActivityBar(ObjectSubclass<imp::ActivityBar>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl ActivityBar {

}