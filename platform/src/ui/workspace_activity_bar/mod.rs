mod imp;

use gtk::glib;
use crate::ActivityBarItem;
use serde::{Deserialize, Serialize};

glib::wrapper! {
    pub struct WorkspaceActivityBar(ObjectSubclass<imp::WorkspaceActivityBar>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
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
        ActivityBarItem::new(&self.icon_name, &self.tooltip, &self.action_name, self.initial_on)
    }
}
