use gtk::{
    glib::{self, once_cell::sync::Lazy, ParamSpec, ParamSpecString, Value},
    prelude::*,
    subclass::prelude::*,
    traits::{BoxExt, WidgetExt}, Align,
};
use platform::ActivityBarItem;
use std::cell::RefCell;

use super::ActivityBarItemJsonObject;

#[derive(Default)]
pub struct WorkspaceActivityBar {
    pub top_box: RefCell<gtk::Box>,
    pub bottom_box: RefCell<gtk::Box>,
}

#[glib::object_subclass]
impl ObjectSubclass for WorkspaceActivityBar {
    const NAME: &'static str = "WorkspaceActivityBar";

    type Type = super::WorkspaceActivityBar;

    type ParentType = gtk::Widget;

    fn class_init(klass: &mut Self::Class) {
        klass.set_layout_manager_type::<gtk::BoxLayout>();
    }
}

impl WorkspaceActivityBar {
    fn parse_json_item(&self, json_data: &str) -> Vec<ActivityBarItem> {
        let activity_bar_item_json_objects: Vec<ActivityBarItemJsonObject> = serde_json::from_str(
            json_data,
        )
        .expect("Serialize `activity-bar-items` json config failed, please check the .ui template");

        activity_bar_item_json_objects
            .iter()
            .map(ActivityBarItemJsonObject::to_activity_bar)
            .collect()
    }
}

impl ObjectImpl for WorkspaceActivityBar {
    fn constructed(&self) {
        self.parent_constructed();

        let obj = self.instance();
        obj.add_css_class("workspace-activity-bar");

        let layout = obj
            .layout_manager()
            .unwrap()
            .downcast::<gtk::BoxLayout>()
            .unwrap();
        layout.set_orientation(gtk::Orientation::Vertical);
        layout.set_homogeneous(true);

        let top_box = self.top_box.borrow();
        let bottom_box = self.bottom_box.borrow();

        top_box.set_parent(&*obj);
        bottom_box.set_parent(&*obj);

        top_box.add_css_class("top-box");
        bottom_box.add_css_class("bottom-box");

        top_box.set_valign(Align::Start);
        bottom_box.set_valign(Align::End);

        top_box.set_orientation(gtk::Orientation::Vertical);
        bottom_box.set_orientation(gtk::Orientation::Vertical);

        top_box.set_hexpand(false);
        bottom_box.set_hexpand(false);

        top_box.set_margin_top(10);
        bottom_box.set_margin_bottom(10);
    }

    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> = Lazy::new(|| {
            vec![
                ParamSpecString::builder("top-box").build(),
                ParamSpecString::builder("bottom-box").build(),
            ]
        });
        PROPERTIES.as_ref()
    }

    fn set_property(&self, _id: usize, value: &Value, pspec: &ParamSpec) {
        match pspec.name() {
            "top-box" => {
                let input_value: String = value
                    .get()
                    .expect("The value needs to be of type `String`.");

                let activity_bar_items: Vec<ActivityBarItem> = self.parse_json_item(&input_value);

                activity_bar_items.iter().for_each(|item| {
                    item.set_width_request(20);
                    item.set_height_request(20);
                    self.top_box.borrow().append(item);
                })
            }
            "bottom-box" => {
                let input_value: String = value
                    .get()
                    .expect("The value needs to be of type `String`.");

                let activity_bar_items: Vec<ActivityBarItem> = self.parse_json_item(&input_value);

                activity_bar_items.iter().for_each(|item| {
                    item.set_width_request(20);
                    item.set_height_request(20);
                    self.bottom_box.borrow().append(item);
                })
            }
            _ => unimplemented!(),
        }
    }

    fn dispose(&self) {
        self.top_box.borrow().unparent();
        self.bottom_box.borrow().unparent();
    }
}

impl WidgetImpl for WorkspaceActivityBar {}
