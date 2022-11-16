use std::cell::RefCell;

use gtk::subclass::prelude::*;
use gtk::glib;

use crate::{FontAwesomeIcon, FontAwesomeStyle};

#[derive(Default)]
pub struct IconButton {
    icon: RefCell<Option<FontAwesomeIcon>>
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

impl ObjectImpl for IconButton {
    fn constructed(&self) {
        self.parent_constructed();

        let code = "f0e0";
        let icon = FontAwesomeIcon::from_code(code, FontAwesomeStyle::FreeRegular);
        icon.set_parent(&*self.instance());
        self.icon.borrow_mut().replace(icon);
    }

    fn dispose(&self) {
        if let Some(icon) = self.icon.take() {
            icon.unparent();
        }
    }
}

impl WidgetImpl for IconButton {}