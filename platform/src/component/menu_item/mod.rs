mod imp;

use gtk::{
    glib::{self, Object},
    prelude::ToValue,
    Label,
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
    label: Option<Label>,
    icon: Option<FontIcon>,
    shortcut: Option<ShortcutLabel>,
    action: Option<String>,
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
        if let Some(ref icon) = self.icon {
            properties.push(("icon", icon));
        }
        if let Some(ref shortcut) = self.shortcut {
            properties.push(("shortcut", shortcut));
        }
        if let Some(ref action) = self.action {
            properties.push(("action", action));
        }

        Object::new(&properties)
    }

    pub fn label(mut self, label: Label) -> Self {
        self.label = Some(label);
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
}
