use std::cell::RefCell;

use gtk::{
    glib::{
        self, clone,
        once_cell::sync::{Lazy, OnceCell},
        ParamSpec, ParamSpecString, Value,
    },
    prelude::*,
    subclass::prelude::*,
    traits::WidgetExt,
};

use crate::{GtkMouseButton, SvgIcon};

#[derive(Default)]
pub struct ActivityBarItem {
    pub svg_icon: RefCell<Option<SvgIcon>>,
    pub icon_name: OnceCell<String>,
}

#[glib::object_subclass]
impl ObjectSubclass for ActivityBarItem {
    const NAME: &'static str = "ActivityBarItem";

    type Type = super::ActivityBarItem;

    type ParentType = gtk::Widget;

    fn class_init(klass: &mut Self::Class) {
        klass.set_layout_manager_type::<gtk::BinLayout>();
    }
}

impl ActivityBarItem {
    pub fn bind_action(&self, action_name: &str) {
        let left_click_gesture = gtk::GestureClick::new();
        left_click_gesture.set_button(GtkMouseButton::LEFT as u32);
        let action_name = action_name.to_string();
        left_click_gesture.connect_released(
            clone!(@weak self as button, @strong action_name => move |gesture, _, _, _| {
                gesture.set_state(gtk::EventSequenceState::Claimed);
                button.instance()
                    .activate_action(action_name.as_str(), None)
                    .expect(format!("Activate action `{}` failed.", action_name).as_str());
            }),
        );
        self.instance().add_controller(&left_click_gesture);
    }
}

impl ObjectImpl for ActivityBarItem {
    fn constructed(&self) {
        self.parent_constructed();

        self.instance().add_css_class("activity-bar-item");
    }

    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> =
            Lazy::new(|| vec![ParamSpecString::builder("icon-name").build()]);
        PROPERTIES.as_ref()
    }

    fn set_property(&self, _id: usize, value: &Value, pspec: &ParamSpec) {
        match pspec.name() {
            "icon-name" => {
                let input_value: String = value
                    .get()
                    .expect("The value needs to be of type `String`.");

                let svg_icon = SvgIcon::new(&input_value);
                svg_icon.set_parent(&*self.instance());

                self.icon_name
                    .set(input_value)
                    .expect("`icon_name` of ActivityBarItem can only set once.");
            }
            _ => unimplemented!(),
        }
    }

    fn property(&self, _id: usize, pspec: &ParamSpec) -> Value {
        match pspec.name() {
            "icon-name" => self
                .icon_name
                .get()
                .expect("`icon_name` of ActivityBarItem is None")
                .to_value(),
            _ => unimplemented!(),
        }
    }
}

impl WidgetImpl for ActivityBarItem {}
