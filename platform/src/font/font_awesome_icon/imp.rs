use std::cell::{Cell, RefCell};

use gtk::{
    glib::{
        self,
        once_cell::sync::{Lazy, OnceCell},
        ParamSpec, ParamSpecString, Value,
    },
    prelude::*,
    subclass::prelude::*,
    Image, Label,
};

use crate::FontAwesomeStyle;

#[derive(Default, Clone, Copy, Debug)]
pub enum FontAwesomeIconType {
    #[default]
    None,
    Code,
    SVG,
}

const STYLE_LIST: [&'static str; 3] = ["Free Regular", "Free Solid", "Brands"];

#[derive(Default)]
pub struct FontAwesomeIcon {
    pub label: RefCell<Option<Label>>,
    pub image: RefCell<Option<Image>>,
    pub code: RefCell<Option<String>>,
    pub svg: RefCell<Option<String>>,
    pub color: RefCell<Option<String>>,
    pub size: Cell<i32>,
    pub icon_type: OnceCell<FontAwesomeIconType>,
    pub font_style: OnceCell<FontAwesomeStyle>,
}

#[glib::object_subclass]
impl ObjectSubclass for FontAwesomeIcon {
    const NAME: &'static str = "FontAwesomeIcon";

    type Type = super::FontAwesomeIcon;
}

impl FontAwesomeIcon {
    pub fn initialize_label(&self) {
        let label = Label::new(None);
        label.set_markup(self.format_markup().as_str());
        self.label.borrow_mut().replace(label);
    }

    pub fn initialize_image(&self) {
        todo!()
    }

    pub fn attribute_change(&self) {
        match self
            .icon_type
            .get()
            .expect("`icon_type` muse be initialized before use.")
        {
            FontAwesomeIconType::Code => {
                self.label
                    .borrow()
                    .as_ref()
                    .expect("`label` of FontAwesomeIcon must be set in `code` mode.")
                    .set_markup(self.format_markup().as_str());
            }
            FontAwesomeIconType::SVG => todo!(),
            _ => unreachable!(),
        }
    }

    pub fn format_markup(&self) -> String {
        let color_ref = self.color.borrow();
        let color = match color_ref.as_ref() {
            Some(color) => color,
            None => "black",
        };

        let style_enum = self
            .font_style
            .get()
            .expect("`font_type` of FontAwesomeIcon must be set in `code` mode.");
        let style = STYLE_LIST[style_enum.to_usize()];

        let size = self.size.get();

        let code_ref = self.code.borrow();
        let code = code_ref
            .as_ref()
            .expect("`code` of FontAwesomeIcon must be set in `code` mode.");

        format!("<span foreground=\"{}\" font_desc=\"Font Awesome 6 {} {}\" font_features=\"dlig=1\">&#x{};</span>", color, style, size, code)
    }
}

impl ObjectImpl for FontAwesomeIcon {
    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> = Lazy::new(|| {
            vec![
                ParamSpecString::builder("code").build(),
                ParamSpecString::builder("svg").build(),
            ]
        });
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
            "svg" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.svg.borrow_mut().replace(input_value);
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
            "svg" => match self.svg.borrow().as_deref() {
                Some(svg) => svg.to_value(),
                None => "".to_value(),
            },
            _ => unimplemented!(),
        }
    }
}
