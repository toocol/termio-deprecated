mod imp;

use gtk::{glib, prelude::*, subclass::prelude::*, traits::WidgetExt, Widget};

glib::wrapper! {
    pub struct BottomStatusBar(ObjectSubclass<imp::BottomStatusBar>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl BottomStatusBar {
    pub fn register_left<T: IsA<Widget>>(&self, widget: &T) {
        widget.set_parent(&*self.imp().left_box.borrow());
    }

    pub fn register_right<T: IsA<Widget>>(&self, widget: &T) {
        widget.set_parent(&*self.imp().right_box.borrow())
    }
}
