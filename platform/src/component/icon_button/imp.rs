use std::cell::{Cell, RefCell};

use gtk::{
    glib::{
        self, clone,
        once_cell::sync::{Lazy, OnceCell},
        ParamSpec, ParamSpecInt, ParamSpecString, Value,
    },
    prelude::*,
    subclass::prelude::*,
    Align, Image,
};
use utilities::DynamicBundle;

use crate::{FontIcon, FontType, GtkMouseButton, IconType, LanguageBundle, SvgIcon};

#[derive(Default)]
pub struct IconButton {
    pub font_icon: RefCell<Option<FontIcon>>,
    pub svg_icon: RefCell<Option<SvgIcon>>,
    pub gtk_icon: RefCell<Option<Image>>,
    pub code: RefCell<Option<String>>,
    pub icon_name: RefCell<Option<String>>,
    pub icon_color: RefCell<Option<String>>,
    pub icon_size: Cell<i32>,
    pub icon_type: OnceCell<IconType>,
    pub action_target: RefCell<Option<String>>,
}

#[glib::object_subclass]
impl ObjectSubclass for IconButton {
    const NAME: &'static str = "IconButton";

    type Type = super::IconButton;

    type ParentType = gtk::Widget;

    fn class_init(klass: &mut Self::Class) {
        // The layout manager determines how child widgets are laid out.
        klass.set_layout_manager_type::<gtk::BinLayout>();

        // Make it look like a GTK button.
        klass.set_css_name("button");

        // Make it appear as a button to accessibility tools.
        klass.set_accessible_role(gtk::AccessibleRole::Button);
    }
}

impl IconButton {
    pub fn property_change(&self) {
        let mut generated = false;
        match self.icon_type.get() {
            None => return,
            Some(IconType::SegoeMDL2) => {
                if let Some(code) = self.code.borrow().as_ref() {
                    let icon = FontIcon::new(code, FontType::SegoeMDL2);
                    icon.set_parent(&*self.instance());
                    self.font_icon.borrow_mut().replace(icon);
                    generated = true;
                }
            }
            Some(IconType::SegoeFluent) => {
                if let Some(code) = self.code.borrow().as_ref() {
                    let icon = FontIcon::new(code, FontType::SegoeFluent);
                    icon.set_parent(&*self.instance());
                    self.font_icon.borrow_mut().replace(icon);
                    generated = true;
                }
            }
            Some(IconType::FontAwesomeFreeRegular) => {
                if let Some(code) = self.code.borrow().as_ref() {
                    let icon = FontIcon::new(code, FontType::FontAwesomeFreeRegular);
                    icon.set_parent(&*self.instance());
                    self.font_icon.borrow_mut().replace(icon);
                    generated = true;
                }
            }
            Some(IconType::FontAwesomeFreeSolid) => {
                if let Some(code) = self.code.borrow().as_ref() {
                    let icon = FontIcon::new(code, FontType::FontAwesomeFreeSolid);
                    icon.set_parent(&*self.instance());
                    self.font_icon.borrow_mut().replace(icon);
                    generated = true;
                }
            }
            Some(IconType::FontAwesomeBrands) => {
                if let Some(code) = self.code.borrow().as_ref() {
                    let icon = FontIcon::new(code, FontType::FontAwesomeBrands);
                    icon.set_parent(&*self.instance());
                    self.font_icon.borrow_mut().replace(icon);
                    generated = true;
                }
            }
            Some(IconType::Svg) => {
                if let Some(icon_name) = self.icon_name.borrow().as_ref() {
                    let icon = SvgIcon::new(icon_name);
                    icon.set_parent(&*self.instance());
                    self.svg_icon.borrow_mut().replace(icon);
                    generated = true;
                }
            }
            Some(IconType::Gtk) => {
                if let Some(icon_name) = self.icon_name.borrow().as_ref() {
                    let icon = Image::builder()
                        .halign(Align::Center)
                        .valign(Align::Center)
                        .icon_name(icon_name)
                        .build();
                    icon.set_parent(&*self.instance());
                    self.gtk_icon.borrow_mut().replace(icon);
                    generated = true;
                }
            }
        }
        if generated {
            self.size_change()
        }
    }

    pub fn size_change(&self) {
        let size = self.icon_size.get();
        match self.icon_type.get() {
            None => panic!("`icon_type` of IconButton is None!"),
            Some(IconType::Svg) => {},
            Some(IconType::Gtk) => {},
            _ => self
                .font_icon
                .borrow()
                .as_ref()
                .expect("`font_awesome_icon` should not be None when IconButton icon type is `FontAwesomeBrands`")
                .set_size(size),
        }
    }

    pub fn color_change(&self) {
        if let Some(color) = self.icon_color.borrow().as_deref() {
            match self.icon_type.get() {
                None => panic!("`icon_type` of IconButton is None!"),
                Some(IconType::Svg) => {},
                Some(IconType::Gtk) => {},
                _ => self
                    .font_icon
                    .borrow()
                    .as_ref()
                    .expect("`font_awesome_icon` should not be None when IconButton icon type is `FontAwesomeBrands`")
                    .set_color(color),
            }
        }
    }

    pub fn bind_action(&self, action_name: &str) {
        let left_click_gesture = gtk::GestureClick::new();
        left_click_gesture.set_button(GtkMouseButton::LEFT as u32);
        let action_name = action_name.to_string();
        left_click_gesture.connect_released(
            clone!(@weak self as button, @strong action_name => move |gesture, _, _, _| {
                gesture.set_state(gtk::EventSequenceState::Claimed);
                let variant;
                let param = if let Some(target) = button.action_target.borrow().as_deref() {
                    variant = target.to_variant();
                    Some(&variant)
                } else {
                    None
                };
                button.instance()
                    .activate_action(action_name.as_str(), param)
                    .expect(format!("Activate action `{}` failed.", action_name).as_str());
            }),
        );
        self.instance().add_controller(&left_click_gesture);
    }
}

impl ObjectImpl for IconButton {
    fn constructed(&self) {
        self.parent_constructed();
        // Set the default icon size to 10
        self.icon_size.set(10);
    }

    fn dispose(&self) {
        if let Some(icon) = self.font_icon.take() {
            icon.unparent();
        }
        if let Some(icon) = self.svg_icon.take() {
            icon.unparent();
        }
        if let Some(icon) = self.gtk_icon.take() {
            icon.unparent();
        }
    }

    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> = Lazy::new(|| {
            vec![
                ParamSpecString::builder("code").build(),
                ParamSpecString::builder("icon-name").build(),
                ParamSpecString::builder("icon-type").build(),
                ParamSpecString::builder("icon-color").build(),
                ParamSpecInt::builder("icon-size").build(),
                ParamSpecString::builder("tooltip").build(),
                ParamSpecString::builder("action-name").build(),
            ]
        });
        PROPERTIES.as_ref()
    }

    fn set_property(&self, _id: usize, value: &Value, pspec: &ParamSpec) {
        match pspec.name() {
            "code" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.code.borrow_mut().replace(input_value);
                self.property_change();
            }
            "icon-name" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.icon_name.borrow_mut().replace(input_value);
                self.property_change();
            }
            "icon-type" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                let icon_type = IconType::from_string(input_value);
                self.icon_type
                    .set(icon_type)
                    .expect("`icon_type` of IconButton can only set once.");
                self.property_change();
            }
            "icon-color" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.icon_color.borrow_mut().replace(input_value);
                self.color_change();
            }
            "icon-size" => {
                let input_value = value.get().expect("The value needs to be of type `i32`.");
                self.icon_size.set(input_value);
                self.size_change();
            }
            "tooltip" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.instance().set_has_tooltip(true);
                self.instance()
                    .set_tooltip_text(Some(LanguageBundle::message(input_value, None).as_str()));
            }
            "action-name" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.bind_action(input_value);
            }
            "action-target" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.action_target.borrow_mut().replace(input_value);
            }
            _ => unimplemented!(),
        }
    }

    fn property(&self, _id: usize, pspec: &ParamSpec) -> Value {
        match pspec.name() {
            "code" => match self.code.borrow().as_deref() {
                Some(code) => code.to_value(),
                None => "".to_value(),
            },
            "icon-name" => match self.icon_name.borrow().as_deref() {
                Some(svg) => svg.to_value(),
                None => "".to_value(),
            },
            "icon-color" => self
                .icon_color
                .borrow()
                .as_deref()
                .unwrap_or("black")
                .to_value(),
            "icon-type" => self
                .icon_type
                .get()
                .expect("`icon_type` of IconButton should initialize first before ues.")
                .to_str()
                .to_value(),
            _ => unimplemented!(),
        }
    }
}

impl WidgetImpl for IconButton {}
