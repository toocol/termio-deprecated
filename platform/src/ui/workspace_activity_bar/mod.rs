mod imp;

use crate::ActivityBarItem;
use gtk::{
    glib,
    prelude::{ObjectType},
    subclass::prelude::ObjectSubclassIsExt,
    traits::WidgetExt,
};
use serde::{Deserialize, Serialize};

glib::wrapper! {
    pub struct WorkspaceActivityBar(ObjectSubclass<imp::WorkspaceActivityBar>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl WorkspaceActivityBar {
    pub fn register_to_top<T: IsActivityBarItem>(&self, widget: &T) {
        widget.to_widget().set_parent(&*self.imp().top_box.borrow())
    }

    pub fn register_to_bottom<T: IsActivityBarItem>(&self, widget: &T) {
        widget
            .to_widget()
            .set_parent(&*self.imp().bottom_box.borrow())
    }
}

pub trait IsActivityBarItem: 'static {
    type Type: ObjectType + WidgetExt;

    fn to_widget(&self) -> &Self::Type;
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
