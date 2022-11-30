mod imp;

use gtk::{glib, prelude::*, subclass::prelude::ObjectSubclassIsExt, Stack};

use crate::ActivityBarItem;

glib::wrapper! {
    pub struct ActivityBar(ObjectSubclass<imp::ActivityBar>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl ActivityBar {
    pub fn set_current_activate_widget(&self, widget_name: Option<String>) {
        *self.imp().activate_widget_name.borrow_mut() = widget_name;
    }

    pub fn hide_activity_bar(&self, left_side_bar: &Stack) {
        if let Some(_) = self.imp().activate_widget_name.borrow().as_ref() {
            left_side_bar.hide();
        }
        self.hide();
    }

    pub fn show_activity_bar(&self, left_side_bar: &Stack) {
        if let Some(activate_widget) = self.imp().activate_widget_name.borrow().as_ref() {
            let item = self
                .top_box_child_by_name(activate_widget.as_str())
                .expect(format!("No widget name represent `{}`", activate_widget).as_str());
            left_side_bar.show();
            item.show();
        }
        self.show();
    }

    pub fn top_box_child_by_name(&self, name: &str) -> Option<ActivityBarItem> {
        for item in self.get_top_box_items() {
            if item.name() == name {
                return Some(item);
            }
        }
        None
    }

    pub fn bottom_box_child_by_name(&self, name: &str) -> Option<ActivityBarItem> {
        for item in self.get_bottom_box_items() {
            if item.name() == name {
                return Some(item);
            }
        }
        None
    }

    pub fn get_top_box_items(&self) -> Vec<ActivityBarItem> {
        let mut items = vec![];
        if let Some(tbox) = self.first_child() {
            if let Some(first_child) = tbox.first_child() {
                let item = first_child
                    .downcast::<ActivityBarItem>()
                    .expect("Downcast ref `ActivityBarItem` filed, type mismatched. ");
                items.push(item);
                let mut first_child = &items[items.len() - 1];
                while let Some(sibling) = first_child.next_sibling() {
                    let item = sibling
                        .downcast::<ActivityBarItem>()
                        .expect("Downcast ref `ActivityBarItem` filed, type mismatched. ");
                    items.push(item);
                    first_child = &items[items.len() - 1];
                }
            }
        }
        items
    }

    pub fn get_bottom_box_items(&self) -> Vec<ActivityBarItem> {
        let mut items = vec![];
        if let Some(tbox) = self.last_child() {
            if let Some(first_child) = tbox.first_child() {
                let item = first_child
                    .downcast::<ActivityBarItem>()
                    .expect("Downcast ref `ActivityBarItem` filed, type mismatched. ");
                items.push(item);
                let mut first_child = &items[items.len() - 1];
                while let Some(sibling) = first_child.next_sibling() {
                    let item = sibling
                        .downcast::<ActivityBarItem>()
                        .expect("Downcast ref `ActivityBarItem` filed, type mismatched. ");
                    items.push(item);
                    first_child = &items[items.len() - 1];
                }
            }
        }
        items
    }

    pub fn toggle_item(&self, widget_name: &str) {
        for item in self.get_top_box_items().iter() {
            if item.name() == widget_name {
                item.toggle_status();
            } else {
                item.set_status_off();
            }
        }
    }

    pub fn set_item_status_off_except(&self, widget_name: &str) {
        for item in self.get_top_box_items().iter() {
            if item.name() == widget_name {
                continue;
            } else {
                item.set_status_off();
            }
        }
    }
}
