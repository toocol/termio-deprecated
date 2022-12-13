use gtk::subclass::prelude::*;
use gtk::glib;

#[derive(Default)]
pub struct MenuModel {

}

#[glib::object_subclass]
impl ObjectSubclass for MenuModel {
    const NAME: &'static str = "MenuModel";

    type Type = super::MenuModel;

    type ParentType = gtk::Widget;
}

impl ObjectImpl for MenuModel {}

impl WidgetImpl for MenuModel {}