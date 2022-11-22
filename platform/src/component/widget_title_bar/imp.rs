use std::cell::{Cell, RefCell};

use gtk::{
    glib::{self, once_cell::sync::Lazy, ParamSpec, ParamSpecBoolean, ParamSpecString, Value},
    prelude::*,
    subclass::prelude::*,
    Align, Label,
};

use crate::{IconButton, IconButtonJsonObject, LanguageBundle};
use crate::prelude::*;

#[derive(Default)]
pub struct WidgetTitleBar {
    left_box: RefCell<gtk::Box>,
    right_box: RefCell<gtk::Box>,
    label: RefCell<Option<Label>>,
    label_text: RefCell<Option<String>>,
    label_font_desc: RefCell<Option<String>>,
    label_bold: Cell<bool>,
    label_italic: Cell<bool>,
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

impl WidgetTitleBar {
    fn label_change(&self) {
        let label_text_opt = self.label_text.borrow();
        let label_text = match label_text_opt.as_deref() {
            Some(label) => label,
            None => "",
        };
        let label_font_famil_opt = self.label_font_desc.borrow();
        let label_font_desc = match label_font_famil_opt.as_deref() {
            Some(font_family) => format!("font_desc=\"{}\"", font_family),
            None => "".to_string(),
        };
        self.label
            .borrow()
            .as_ref()
            .expect("`label` of WidgetTitleBar is None.")
            .set_markup(
                format!(
                    "<span {}>{}{}{}{}{}</span>",
                    label_font_desc,
                    if self.label_italic.get() { "<i>" } else { "" },
                    if self.label_bold.get() { "<b>" } else { "" },
                    LanguageBundle::message(label_text, None),
                    if self.label_bold.get() { "</b>" } else { "" },
                    if self.label_italic.get() { "</i>" } else { "" },
                )
                .as_str(),
            );
    }
}

impl ObjectImpl for WidgetTitleBar {
    fn constructed(&self) {
        self.parent_constructed();

        // Set default `bold` to True
        self.label_bold.set(true);

        let obj = self.instance();
        obj.add_css_class("widget-title-bar");

        let layout = obj
            .layout_manager()
            .unwrap()
            .downcast::<gtk::BoxLayout>()
            .unwrap();
        layout.set_orientation(gtk::Orientation::Horizontal);
        layout.set_spacing(5);

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

        let label = Label::builder()
            .use_markup(true)
            .margin_start(10)
            .margin_end(10)
            .build();
        left_box.append(&label);
        self.label.borrow_mut().replace(label);
    }

    fn dispose(&self) {
        self.left_box.borrow().unparent();
        self.right_box.borrow().unparent();
    }

    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> = Lazy::new(|| {
            vec![
                ParamSpecString::builder("label").build(),
                ParamSpecString::builder("label-font-desc").build(),
                ParamSpecBoolean::builder("label-bold").build(),
                ParamSpecBoolean::builder("label-italic").build(),
                ParamSpecString::builder("control-icon-buttons").build(),
            ]
        });
        PROPERTIES.as_ref()
    }

    fn set_property(&self, _id: usize, value: &Value, pspec: &ParamSpec) {
        match pspec.name() {
            "label" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.label_text.borrow_mut().replace(input_value);
                self.label_change();
            }
            "label-font-desc" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.label_font_desc.borrow_mut().replace(input_value);
                self.label_change();
            }
            "label-bold" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.label_bold.set(input_value);
                self.label_change();
            }
            "label-italic" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.label_italic.set(input_value);
                self.label_change();
            }
            "control-icon-buttons" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");

                let icon_button_json_objects: Vec<IconButtonJsonObject> = serde_json::from_str(input_value)
                    .expect("Serialize `control-icon-buttons` json config failed, please check the .ui template");

                let icon_buttons: Vec<IconButton> = icon_button_json_objects
                    .iter()
                    .map(IconButtonJsonObject::to_icon_button)
                    .collect();

                icon_buttons.iter().for_each(|icon_button| {
                    let wrap_box = gtk::Box::builder()
                        .width_request(5)
                        .height_request(5)
                        .build();
                    icon_button.add_css_class("widget-title-control-button");
                    wrap_box.append(icon_button);
                    self.right_box.borrow().append(&wrap_box);
                })
            }
            _ => unimplemented!(),
        }
    }

    fn property(&self, _id: usize, pspec: &ParamSpec) -> Value {
        match pspec.name() {
            "label" => match self.label.borrow().as_ref() {
                Some(code) => code.label().to_value(),
                None => "".to_value(),
            },
            "label-font-desc" => match self.label_font_desc.borrow().as_deref() {
                Some(font_family) => font_family.to_value(),
                None => "".to_value(),
            },
            "label-bold" => self.label_bold.get().to_value(),
            "label-italic" => self.label_italic.get().to_value(),
            _ => unimplemented!(),
        }
    }
}

impl WidgetImpl for WidgetTitleBar {}
