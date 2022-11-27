mod imp;

use glib::Object;
use gtk::glib;
use serde::{Deserialize, Serialize};

use crate::IconButton;

glib::wrapper! {
    pub struct WidgetTitleBar(ObjectSubclass<imp::WidgetTitleBar>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl WidgetTitleBar {
    pub fn new() -> Self {
        Object::builder().build()
    }
}

#[derive(Serialize, Deserialize)]
pub struct IconButtonJsonObject {
    pub icon_type: String,
    pub code: Option<String>,
    pub icon_name: Option<String>,
    pub tooltip: Option<String>,
    pub action_name: Option<String>,
    pub action_target: Option<String>,
}

impl IconButtonJsonObject {
    pub fn to_icon_button(&self) -> IconButton {
        IconButton::new(
            &self.icon_type,
            &self.code,
            &self.icon_name,
            &self.tooltip,
            &self.action_name,
            &self.action_target,
        )
    }
}
