use gtk::{
    glib,
    glib::{
        once_cell::sync::{Lazy, OnceCell},
        ParamSpec, ParamSpecString, Value,
    },
    prelude::*,
    subclass::prelude::*,
    Label,
};
use std::cell::{Cell, RefCell};

use crate::FontType;

const FONT_FAMILIES: [&'static str; 5] = [
    "Segoe MDL2 Assets",
    "Segoe Fluent Icons",
    "Font Awesome 6 Free Regular",
    "Font Awesome 6 Free Solid",
    "Font Awesome 6 Brands",
];

#[derive(Default)]
pub struct FontIcon {
    pub label: RefCell<Label>,
    pub code: RefCell<Option<String>>,
    pub color: RefCell<Option<String>>,
    pub size: Cell<i32>,
    pub bold: Cell<bool>,
    pub font_type: OnceCell<FontType>,
}

impl FontIcon {
    pub fn attribute_change(&self) {
        self.label
            .borrow()
            .set_markup(self.format_markup().as_str());
    }

    pub fn format_markup(&self) -> String {
        let color_ref = self.color.borrow();
        let color = match color_ref.as_ref() {
            Some(color) => color,
            None => "black",
        };

        let type_enum = self
            .font_type
            .get()
            .expect("`font_type` of SegoeFontIcon must be set in `code` mode.");
        let style = FONT_FAMILIES[type_enum.to_usize()];

        let size = self.size.get();

        let code_ref = self.code.borrow();
        let code = code_ref
            .as_ref()
            .expect("`code` of SegoeFontIcon must be set in `code` mode.");

        format!(
            "<span foreground=\"{}\" font_desc=\"{} {}\" font_features=\"dlig=1\">{}&#x{};{}</span>",
            color, 
            style, 
            size, 
            if self.bold.get() { "<b>" } else { "" }, 
            code, 
            if self.bold.get() { "</b>" } else { "" }
        )
    }
}

#[glib::object_subclass]
impl ObjectSubclass for FontIcon {
    const NAME: &'static str = "SegoeFontIcon";

    type Type = super::FontIcon;
}

impl ObjectImpl for FontIcon {
    fn constructed(&self) {
        self.parent_constructed();
        // Set the default size
        self.size.set(10);
    }

    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> =
            Lazy::new(|| vec![ParamSpecString::builder("code").build()]);
        PROPERTIES.as_ref()
    }

    fn set_property(&self, _id: usize, value: &Value, pspec: &ParamSpec) {
        match pspec.name() {
            "code" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.code.borrow_mut().replace(input_value);
            }
            _ => unimplemented!(),
        }
    }

    fn property(&self, _id: usize, pspec: &ParamSpec) -> Value {
        match pspec.name() {
            "code" => match self.code.borrow().as_deref() {
                Some(code) => code.to_value(),
                None => "".to_value(),
            },
            _ => unimplemented!(),
        }
    }
}
