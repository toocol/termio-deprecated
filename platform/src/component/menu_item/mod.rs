mod imp;

use gtk::{
    glib::{self, Object},
    prelude::ToValue,
};

use crate::{FontIcon, ShortcutLabel};

glib::wrapper! {
    pub struct MenuItem(ObjectSubclass<imp::MenuItem>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl MenuItem {
    pub fn builder() -> MenuItemBuilder {
        MenuItemBuilder::new()
    }
}

#[derive(Default)]
pub struct MenuItemBuilder {
    label: Option<String>,
    markup: Option<String>,
    icon: Option<FontIcon>,
    shortcut: Option<ShortcutLabel>,
    action: Option<String>,
    action_param: Option<String>,
}

impl MenuItemBuilder {
    pub fn new() -> Self {
        MenuItemBuilder::default()
    }

    pub fn build(self) -> MenuItem {
        let mut properties: Vec<(&'static str, &dyn ToValue)> = vec![];

        if let Some(ref label) = self.label {
            properties.push(("label", label));
        }
        if let Some(ref markup) = self.label {
            properties.push(("markup", markup));
        }
        if let Some(ref icon) = self.icon {
            properties.push(("icon", icon));
        }
        if let Some(ref shortcut) = self.shortcut {
            properties.push(("shortcut", shortcut));
        }
        if let Some(ref action) = self.action {
            properties.push(("action", action));
        }
        if let Some(ref action_param) = self.action_param {
            properties.push(("action-param", action_param));
        }

        Object::new(&properties)
    }

    pub fn label(mut self, label: &str) -> Self {
        self.label = Some(label.to_string());
        self
    }

    pub fn markup(mut self, markup: &str) -> Self {
        self.markup = Some(markup.to_string());
        self
    }

    pub fn icon(mut self, icon: FontIcon) -> Self {
        self.icon = Some(icon);
        self
    }

    pub fn shortcut(mut self, shortcut: ShortcutLabel) -> Self {
        self.shortcut = Some(shortcut);
        self
    }

    pub fn action(mut self, action: &str) -> Self {
        self.action = Some(action.to_string());
        self
    }

    pub fn action_param(mut self, action_param: &str) -> Self {
        self.action_param = Some(action_param.to_string());
        self
    }
}
