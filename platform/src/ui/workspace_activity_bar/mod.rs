mod imp;

use crate::ActivityBarItem;
use gtk::{
    glib,
    prelude::IsA,
    subclass::prelude::ObjectSubclassIsExt,
    traits::WidgetExt,
    Widget,
};
use serde::{Deserialize, Serialize};

glib::wrapper! {
    pub struct WorkspaceActivityBar(ObjectSubclass<imp::WorkspaceActivityBar>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl WorkspaceActivityBar {
    pub fn register_to_top<T: IsA<Widget>>(&self, widget: &T) {
        widget.set_parent(&*self.imp().top_box.borrow())
    }

    pub fn register_to_bottom<T: IsA<Widget>>(&self, widget: &T) {
        widget.set_parent(&*self.imp().bottom_box.borrow())
    }
}

#[derive(Serialize, Deserialize)]
pub struct ActivityBarItemJsonObject {
    pub icon_name: String,
    pub initial_on: bool,
    pub tooltip: Option<String>,
    pub action_name: Option<String>,
}

impl ActivityBarItemJsonObject {
    pub fn to_activity_bar(&self) -> ActivityBarItem {
        ActivityBarItem::new(
            &self.icon_name,
            &self.tooltip,
            &self.action_name,
            self.initial_on,
        )
    }
}
