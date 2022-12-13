use gtk::subclass::prelude::*;
use gtk::glib;

#[derive(Default)]
pub struct MenuItem {}

#[glib::object_subclass]
impl ObjectSubclass for MenuItem {
    const NAME: &'static str = "MenuItem";

    type Type = super::MenuItem;

    type ParentType = gtk::Widget;
}

impl ObjectImpl for MenuItem {}

impl WidgetImpl for MenuItem {}