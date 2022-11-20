mod imp;

use gtk::glib;

use crate::IsBottomStatusBarItem;

glib::wrapper! {
    pub struct EditionMark(ObjectSubclass<imp::EditionMark>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl IsBottomStatusBarItem for EditionMark {
    type Type = super::EditionMark;

    fn to_widget(&self) -> &Self::Type {
        self
    }
}
