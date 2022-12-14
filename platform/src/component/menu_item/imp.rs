use std::cell::RefCell;

use gtk::{
    glib::{
        self, clone,
        once_cell::sync::{Lazy, OnceCell},
        ParamSpec, ParamSpecObject, ParamSpecString, Value,
    },
    prelude::*,
    subclass::prelude::*,
    Align, GestureClick, Label,
};

use crate::{FontIcon, GtkMouseButton, ShortcutLabel};

#[derive(Default)]
pub struct MenuItem {
    left_box: RefCell<gtk::Box>,
    right_box: RefCell<gtk::Box>,

    pub icon: OnceCell<FontIcon>,
    pub label: OnceCell<Label>,
    pub shortcut: OnceCell<ShortcutLabel>,
}

#[glib::object_subclass]
impl ObjectSubclass for MenuItem {
    const NAME: &'static str = "MenuItem";

    type Type = super::MenuItem;

    type ParentType = gtk::Widget;

    fn class_init(klass: &mut Self::Class) {
        klass.set_layout_manager_type::<gtk::BoxLayout>();
    }
}

impl ObjectImpl for MenuItem {
    fn constructed(&self) {
        self.parent_constructed();
        let obj = self.instance();
        obj.add_css_class("menu-item");

        let layout = self
            .instance()
            .layout_manager()
            .unwrap()
            .downcast::<gtk::BoxLayout>()
            .unwrap();
        layout.set_orientation(gtk::Orientation::Horizontal);
        layout.set_spacing(0);

        let left_box = self.left_box.borrow();
        let right_box = self.right_box.borrow();

        left_box.set_parent(&*obj);
        right_box.set_parent(&*obj);

        left_box.add_css_class("left-box");
        right_box.add_css_class("right-box");

        left_box.set_halign(Align::Start);
        right_box.set_halign(Align::End);

        left_box.set_orientation(gtk::Orientation::Horizontal);
        right_box.set_orientation(gtk::Orientation::Horizontal);

        left_box.set_hexpand(true);
        right_box.set_hexpand(true);

        left_box.set_spacing(10);
        right_box.set_spacing(10);

        left_box.set_margin_start(5);
        right_box.set_margin_end(5);
    }

    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> = Lazy::new(|| {
            vec![
                ParamSpecString::builder("label").build(),
                ParamSpecString::builder("markup").build(),
                ParamSpecObject::builder::<FontIcon>("icon").build(),
                ParamSpecObject::builder::<ShortcutLabel>("shortcut").build(),
                ParamSpecString::builder("action").build(),
            ]
        });
        PROPERTIES.as_ref()
    }

    fn set_property(&self, _id: usize, value: &Value, pspec: &ParamSpec) {
        match pspec.name() {
            "label" => {
                let input_value = value.get().expect("The value needs to be of type `Label`.");
                if let None = self.label.get() {
                    let label = Label::builder().use_markup(true).build();
                    self.left_box.borrow().append(&label);
                    self.label
                        .set(label)
                        .expect("`label` of `MenuItem` can only set once.");
                }
                self.label.get().unwrap().set_label(input_value);
            }
            "markup" => {
                let input_value = value.get().expect("The value needs to be of type `Label`.");
                if let None = self.label.get() {
                    let label = Label::builder().use_markup(true).build();
                    self.left_box.borrow().append(&label);
                    self.label
                        .set(label)
                        .expect("`label` of `MenuItem` can only set once.");
                }
                self.label.get().unwrap().set_markup(input_value);
            }
            "icon" => {
                let input_value: FontIcon =
                    value.get().expect("The value needs to be of type `Icon`.");
                input_value.with_label(clone!(@weak self as item => move |label| {
                    item.left_box.borrow().prepend(label);
                }));
                self.icon
                    .set(input_value)
                    .expect("`label` of `MenuItem` can only set once.");
            }
            "shortcut" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `ShortcutLabel`.");
                self.right_box.borrow().append(&input_value);
                self.shortcut
                    .set(input_value)
                    .expect("`label` of `MenuItem` can only set once.");
            }
            "action" => {
                let input_value: String = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                let gesture_click = GestureClick::new();
                gesture_click.set_button(GtkMouseButton::Left as u32);
                gesture_click.connect_pressed(clone!(@weak self as item => move |_, _, _, _| {
                    item.instance()
                        .activate_action(input_value.as_str(), None)
                        .expect(format!("Activate action failed {}", input_value).as_str());
                }));
                self.instance().add_controller(&gesture_click);
            }
            _ => unimplemented!(),
        }
    }

    fn dispose(&self) {
        if let Some(icon) = self.icon.get() {
            icon.unparent();
        }
        if let Some(label) = self.label.get() {
            label.unparent();
        }
        if let Some(shortcut) = self.shortcut.get() {
            shortcut.unparent();
        }
        self.left_box.borrow().unparent();
        self.right_box.borrow().unparent();
    }
}

impl WidgetImpl for MenuItem {}
