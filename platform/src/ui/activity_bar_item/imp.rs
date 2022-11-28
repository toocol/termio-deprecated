use std::cell::{Cell, RefCell};

use gtk::{
    glib::{
        self, clone,
        once_cell::sync::{Lazy, OnceCell},
        ParamSpec, ParamSpecBoolean, ParamSpecInt, ParamSpecString, Value,
    },
    prelude::*,
    subclass::prelude::*,
};
use utilities::DynamicBundle;

use crate::{FontIcon, GtkMouseButton, ItemStatus, LanguageBundle, ItemPosition};

const CSS_CLASS: &str = "activity-bar-item";
const STATUS_ON_CSS: &str = "activity-bar-item-on";
const STATUS_OFF_CSS: &str = "activity-bar-item-off";

#[derive(Default)]
pub struct ActivityBarItem {
    pub font_icon: RefCell<Option<FontIcon>>,
    pub code: OnceCell<String>,
    pub bind_widget_name: OnceCell<String>,
    pub position: OnceCell<ItemPosition>,
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
            }
            ItemStatus::On => {
                self.status.set(ItemStatus::Off);
                self.instance().remove_css_class(STATUS_ON_CSS);
                self.instance().add_css_class(STATUS_OFF_CSS);
            }
        }
    }

    pub fn set_status_off(&self) {
        self.status.set(ItemStatus::Off);
        self.instance().remove_css_class(STATUS_ON_CSS);
        self.instance().add_css_class(STATUS_OFF_CSS);
    }

    pub fn activate(&self) {
        self.status.set(ItemStatus::On);
        self.instance().remove_css_class(STATUS_OFF_CSS);
        self.instance().add_css_class(STATUS_ON_CSS);
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
                let param = (item.instance().name(), item.status.get().to_u8());
                item.instance()
                    .activate_action(action_name.as_str(), Some(&param.to_variant()))
                    .expect(format!("Activate action `{}` failed.", action_name).as_str());
            }),
        );
        self.instance().add_controller(&left_click_gesture);
    }
}

impl ObjectImpl for ActivityBarItem {
    fn constructed(&self) {
        self.parent_constructed();

        self.instance().add_css_class(CSS_CLASS);
        self.instance().add_css_class(STATUS_OFF_CSS);
    }

    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> = Lazy::new(|| {
            vec![
                ParamSpecString::builder("code").build(),
                ParamSpecString::builder("action-name").build(),
                ParamSpecBoolean::builder("initial-on").build(),
                ParamSpecInt::builder("icon-size").build(),
                ParamSpecString::builder("tooltip").build(),
                ParamSpecString::builder("bind-widget-name").build(),
                ParamSpecString::builder("position").build(),
            ]
        });
        PROPERTIES.as_ref()
    }

    fn set_property(&self, _id: usize, value: &Value, pspec: &ParamSpec) {
        match pspec.name() {
            "code" => {
                let input_value: String = value
                    .get()
                    .expect("The value needs to be of type `String`.");

                let font_icon = FontIcon::new(&input_value, crate::FontType::SegoeFluent);
                font_icon.set_parent(&*self.instance());
                self.font_icon.borrow_mut().replace(font_icon);

                self.code
                    .set(input_value)
                    .expect("`icon_name` of ActivityBarItem can only set once.");
            }
            "action-name" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.bind_action(input_value);
            }
            "initial-on" => {
                let initial_on: bool = value.get().expect("The value needs to be of type `bool`.");
                if initial_on {
                    self.activate()
                }
            }
            "icon-size" => {
                let input_value = value.get().expect("The value needs to be of type `i32`.");
                self.font_icon
                    .borrow()
                    .as_ref()
                    .expect("`font_icon` of ActivityBarItem is None.")
                    .set_size(input_value);
            }
            "tooltip" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.instance().set_has_tooltip(true);
                self.instance()
                    .set_tooltip_text(Some(LanguageBundle::message(input_value, None).as_str()));
            }
            "bind-widget-name" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.bind_widget_name
                    .set(input_value)
                    .expect("`bind_widget_name` can only set once.");
            }
            "position" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.position
                    .set(ItemPosition::from_str(input_value))
                    .expect("`position` can only set once.");
            }
            _ => unimplemented!(),
        }
    }

    fn property(&self, _id: usize, pspec: &ParamSpec) -> Value {
        match pspec.name() {
            "code" => self
                .code
                .get()
                .expect("`icon_name` of ActivityBarItem is None")
                .to_value(),
            _ => unimplemented!(),
        }
    }

    fn dispose(&self) {
        if let Some(icon) = self.font_icon.borrow().as_ref() {
            icon.unparent();
        }
    }
}

impl WidgetImpl for ActivityBarItem {}
