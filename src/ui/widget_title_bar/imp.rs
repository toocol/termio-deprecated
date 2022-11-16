use std::cell::RefCell;

use gtk::{glib, prelude::*, subclass::prelude::*, Align, Label};

#[derive(Default)]
pub struct WidgetTitleBar {
    left_box: RefCell<gtk::Box>,
    right_box: RefCell<gtk::Box>,
}

#[glib::object_subclass]
impl ObjectSubclass for WidgetTitleBar {
    const NAME: &'static str = "WidgetTitleBar";

    type Type = super::WidgetTitleBar;

    type ParentType = gtk::Widget;

    fn class_init(klass: &mut Self::Class) {
        // The layout manager determines how child widgets are laid out.
        klass.set_layout_manager_type::<gtk::BoxLayout>();
    }
}

impl ObjectImpl for WidgetTitleBar {
    fn constructed(&self) {
        self.parent_constructed();

        let obj = self.instance();
        obj.add_css_class("widget-title-bar");

        let layout = obj
            .layout_manager()
            .unwrap()
            .downcast::<gtk::BoxLayout>()
            .unwrap();
        layout.set_orientation(gtk::Orientation::Horizontal);
        layout.set_spacing(5);
        layout.set_homogeneous(true);

        let left_box = self.left_box.borrow();
        let right_box = self.right_box.borrow();
        left_box.add_css_class("left-box");
        right_box.add_css_class("right-box");

        left_box.set_parent(&*obj);
        right_box.set_parent(&*obj);

        left_box.set_halign(Align::Start);
        right_box.set_halign(Align::End);
        left_box.set_orientation(gtk::Orientation::Horizontal);
        right_box.set_orientation(gtk::Orientation::Horizontal);
        left_box.set_hexpand(false);
        right_box.set_hexpand(false);

        let label = Label::builder()
            .use_markup(true)
            .margin_start(10)
            .margin_end(10)
            .build();
        label.set_markup("<span font_desc=\"Consolas\"><b>SESSION MANAGEMENT</b></span>");
        left_box.append(&label);

        let button_add_box = gtk::Box::builder()
            .width_request(5)
            .height_request(5)
            .build();
        let button_add = gtk::Button::builder()
            .icon_name("list-add-symbolic")
            .tooltip_text("Button add Tooltip")
            .halign(Align::Center)
            .build();
        let button_minus = gtk::Button::builder()
            .icon_name("list-remove-symbolic")
            .tooltip_text("Button add Tooltip")
            .halign(Align::Center)
            .build();
        button_add.add_css_class("widget-title-control-button");
        button_minus.add_css_class("widget-title-control-button");
        button_add_box.append(&button_add);
        button_add_box.append(&button_minus);
        right_box.append(&button_add_box);
    }

    fn dispose(&self) {
        self.left_box.borrow().unparent();
        self.right_box.borrow().unparent();
    }
}

impl WidgetImpl for WidgetTitleBar {}
