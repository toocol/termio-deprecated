use std::cell::{Cell, RefCell};

use gtk::{
    glib::{
        self, clone,
        once_cell::sync::{Lazy, OnceCell},
        ParamSpec, ParamSpecString, Value,
    },
    prelude::*,
    subclass::prelude::*,
};

use crate::{GtkMouseButton, ItemStatus, SvgIcon};

const STATUS_ON_CSS: &str = "activity-bar-item-on";
const STATUS_OFF_CSS: &str = "activity-bar-item-off";

#[derive(Default)]
pub struct ActivityBarItem {
    pub svg_icon: RefCell<Option<SvgIcon>>,
    pub icon_name: OnceCell<String>,
    pub status: Cell<ItemStatus>,
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
    pub fn toggle_status(&self) {
        match self.status.get() {
            ItemStatus::Off => {
                self.status.set(ItemStatus::On);
                self.instance().remove_css_class(STATUS_OFF_CSS);
                self.instance().add_css_class(STATUS_ON_CSS);
            },
            ItemStatus::On => {
                self.status.set(ItemStatus::Off);
                self.instance().remove_css_class(STATUS_ON_CSS);
                self.instance().add_css_class(STATUS_OFF_CSS);
            },
        }
    }
}

impl ActivityBarItem {
    pub fn bind_action(&self, action_name: &str) {
        let left_click_gesture = gtk::GestureClick::new();
        left_click_gesture.set_button(GtkMouseButton::LEFT as u32);
        let action_name = action_name.to_string();
        left_click_gesture.connect_released(
            clone!(@weak self as item, @strong action_name => move |gesture, _, _, _| {
                gesture.set_state(gtk::EventSequenceState::Claimed);
                item.toggle_status();
                item.instance()
                    .activate_action(action_name.as_str(), Some(&item.status.get().to_u8().to_variant()))
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
        self.instance().add_css_class(STATUS_OFF_CSS);
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
                self.svg_icon.borrow_mut().replace(svg_icon);

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

    fn dispose(&self) {
        if let Some(icon) = self.svg_icon.borrow().as_ref() {
            icon.unparent();
        }
    }
}

impl WidgetImpl for ActivityBarItem {}
