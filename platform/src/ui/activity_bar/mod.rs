mod imp;

use gtk::{glib, prelude::*};

use crate::ActivityBarItem;

glib::wrapper! {
    pub struct ActivityBar(ObjectSubclass<imp::ActivityBar>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl ActivityBar {
    pub fn get_all_items(&self) -> Vec<ActivityBarItem> {
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

            while let Some(tbox) = tbox.next_sibling() {
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
        }
        items
    }
}
