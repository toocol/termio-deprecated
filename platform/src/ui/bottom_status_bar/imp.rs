use std::cell::RefCell;

use gtk::{glib, prelude::*, subclass::prelude::*, traits::WidgetExt, Align, Orientation};

#[derive(Default)]
pub struct BottomStatusBar {
    pub left_box: RefCell<gtk::Box>,
    pub right_box: RefCell<gtk::Box>,
}

#[glib::object_subclass]
impl ObjectSubclass for BottomStatusBar {
    const NAME: &'static str = "BottomStatusBar";

    type Type = super::BottomStatusBar;

    type ParentType = gtk::Widget;

    fn class_init(klass: &mut Self::Class) {
        // The layout manager determines how child widgets are laid out.
        klass.set_layout_manager_type::<gtk::BoxLayout>();
    }
}

impl ObjectImpl for BottomStatusBar {
    fn constructed(&self) {
        self.parent_constructed();

        let obj = self.instance();

        let layout = obj
            .layout_manager()
            .unwrap()
            .downcast::<gtk::BoxLayout>()
            .unwrap();
        layout.set_orientation(gtk::Orientation::Horizontal);
        layout.set_homogeneous(true);

        let left_box = self.left_box.borrow();
        let right_box = self.right_box.borrow();

        left_box.set_spacing(5);
        right_box.set_spacing(5);

        left_box.set_parent(&*obj);
        right_box.set_parent(&*obj);

        left_box.add_css_class("left-box");
        right_box.add_css_class("right-box");

        left_box.set_halign(Align::Start);
        right_box.set_halign(Align::End);

        left_box.set_orientation(Orientation::Horizontal);
        right_box.set_orientation(Orientation::Horizontal);

        left_box.set_hexpand(false);
        right_box.set_hexpand(false);
    }

    fn dispose(&self) {
        self.left_box.borrow().unparent();
        self.right_box.borrow().unparent();
    }
}

impl WidgetImpl for BottomStatusBar {}
