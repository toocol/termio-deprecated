use gtk::glib::once_cell::sync::OnceCell;
use gtk::subclass::prelude::*;
use gtk::glib;
use gtk::traits::WidgetExt;

use crate::MenuModel;

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
        self.instance().add_css_class("shell-startup-menu");

        let menu_model = MenuModel::new();
        menu_model.set_parent(&*self.instance());
        self.model.set(menu_model).expect("`model` of `ShellStartupMenu` can only set once.")
    }

    fn dispose(&self) {
        if let Some(model) = self.model.get() {
            model.unparent();
        }
    }
}

impl WidgetImpl for ShellStartupMenu {}

impl PopoverImpl for ShellStartupMenu {}