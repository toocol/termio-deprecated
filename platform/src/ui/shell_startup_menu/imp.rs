use gtk::glib;
use gtk::subclass::prelude::*;
use gtk::traits::WidgetExt;
use gtk::{glib::once_cell::sync::OnceCell, Label};

use crate::{MenuItem, MenuModel};

#[derive(Default)]
pub struct ShellStartupMenu {
    pub model: OnceCell<MenuModel>,
}

#[glib::object_subclass]
impl ObjectSubclass for ShellStartupMenu {
    const NAME: &'static str = "ShellStartupMenu";

    type Type = super::ShellStartupMenu;

    type ParentType = gtk::Popover;
}

impl ObjectImpl for ShellStartupMenu {
    fn constructed(&self) {
        self.parent_constructed();
        let obj = self.instance();
        obj.add_css_class("shell-startup-menu");

        let menu_model = MenuModel::new();
        menu_model.set_parent(&*self.instance());
        self.model
            .set(menu_model)
            .expect("`model` of `ShellStartupMenu` can only set once.");

        let item = MenuItem::builder()
            .label(Label::new(Some("Command Prompt")))
            .build();
        obj.append_item(item);
        let item = MenuItem::builder()
            .label(Label::new(Some("Windows PowerShell")))
            .build();
        obj.append_item(item);
    }

    fn dispose(&self) {
        if let Some(model) = self.model.get() {
            model.unparent();
        }
    }
}

impl WidgetImpl for ShellStartupMenu {}

impl PopoverImpl for ShellStartupMenu {}
