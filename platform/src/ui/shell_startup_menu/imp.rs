use gtk::subclass::prelude::*;
use gtk::glib;

#[derive(Default)]
pub struct ShellStartupMenu {}

#[glib::object_subclass]
impl ObjectSubclass for ShellStartupMenu {
    const NAME: &'static str = "ShellStartupMenu";

    type Type = super::ShellStartupMenu;

    type ParentType = gtk::Popover;
}

impl ObjectImpl for ShellStartupMenu {

}

impl WidgetImpl for ShellStartupMenu {

}

impl PopoverImpl for ShellStartupMenu {

}