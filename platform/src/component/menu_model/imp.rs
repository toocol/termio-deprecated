use std::cell::RefCell;

use gtk::{glib, prelude::*, subclass::prelude::*};

use crate::{MenuItem, MenuSection};

#[derive(Default)]
pub struct MenuModel {
    items: RefCell<Vec<MenuItem>>,
    sections: RefCell<Vec<MenuSection>>,
}

impl MenuModel {
    pub fn append_item(&self, item: MenuItem) {
        item.set_parent(&*self.instance());
        self.items.borrow_mut().push(item);
    }

    pub fn append_section(&self, section: MenuSection) {
        section.set_parent(&*self.instance());
        self.sections.borrow_mut().push(section);
    }
}

#[glib::object_subclass]
impl ObjectSubclass for MenuModel {
    const NAME: &'static str = "MenuModel";

    type Type = super::MenuModel;

    type ParentType = gtk::Widget;

    fn class_init(klass: &mut Self::Class) {
        klass.set_layout_manager_type::<gtk::BoxLayout>();
    }
}

impl ObjectImpl for MenuModel {
    fn constructed(&self) {
        self.parent_constructed();
        self.instance().add_css_class("menu-model");

        let layout = self
            .instance()
            .layout_manager()
            .unwrap()
            .downcast::<gtk::BoxLayout>()
            .unwrap();
        layout.set_orientation(gtk::Orientation::Vertical);
    }

    fn dispose(&self) {
        for item in self.items.borrow().iter() {
            item.unparent();
        }
        for section in self.sections.borrow().iter() {
            section.unparent();
        }
    }
}

impl WidgetImpl for MenuModel {}
