mod imp;

use gtk::{glib, prelude::*, subclass::prelude::*, traits::WidgetExt};

glib::wrapper! {
    pub struct BottomStatusBar(ObjectSubclass<imp::BottomStatusBar>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl BottomStatusBar {
    pub fn register_to_left<T: IsBottomStatusBarItem>(&self, widget: &T) {
        widget
            .to_widget()
            .set_parent(&*self.imp().left_box.borrow())
    }

    pub fn register_to_right<T: IsBottomStatusBarItem>(&self, widget: &T) {
        widget
            .to_widget()
            .set_parent(&*self.imp().right_box.borrow())
    }
}

pub trait IsBottomStatusBarItem: 'static {
    type Type: ObjectType + WidgetExt;

    fn to_widget(&self) -> &Self::Type;
}
