use std::{cell::Cell, f64::consts::PI};

use gtk::{
    glib::{
        self, clone, once_cell::sync::Lazy, ParamSpec, ParamSpecDouble, ParamSpecString, Value,
    },
    prelude::*,
    subclass::prelude::*,
};
use libs::Color;

const DEFAULT_FACTOR: f64 = 0.8;
const DEFAULT_SPINED: f64 = 1.5;
const DEFAULT_MARGIN: f64 = 15.;
const DEFAULT_COLOR: (f64, f64, f64) = (0.2, 0.2, 0.2);

pub struct DotSeperator {
    pub factor: Cell<f64>,
    pub spined: Cell<f64>,
    pub margin: Cell<f64>,
    pub color: Cell<(f64, f64, f64)>,
}

impl DotSeperator {
    pub fn update_draw_func(&self) {
        self.instance().set_draw_func(
            clone!(@weak self as seperator => move |_, cr, width, height| {
            let width = width as f64;
            let height = height as f64;

            let single = width / 2. * seperator.factor.get();
            let spined = seperator.spined.get();
            let margin = seperator.margin.get();
            let ver = (height - 1.) / 2.;

            // Background
            cr.set_source_rgba(0.0, 0.0, 0.0, 0.0);
            cr.paint().unwrap();

            cr.set_source_rgba(0.5, 0.5, 0.5, 0.8);
            cr.set_line_width(0.5);

            let start = (width - (single * 2. + margin * 2. + spined)) / 2.;

            cr.move_to(start, ver);
            cr.line_to(start + single, ver);
            cr.stroke().unwrap();

            cr.move_to(start + single + margin * 2. + spined, ver);
            cr.line_to(width - start, ver);
            cr.stroke().unwrap();

            cr.set_source_rgba(0.5, 0.5, 0.5, 0.5);
            cr.arc(width / 2., ver, spined, -PI, PI);
            cr.fill().unwrap();
            }),
        )
    }
}

impl Default for DotSeperator {
    fn default() -> Self {
        Self {
            factor: Cell::new(DEFAULT_FACTOR),
            spined: Cell::new(DEFAULT_SPINED),
            margin: Cell::new(DEFAULT_MARGIN),
            color: Cell::new(DEFAULT_COLOR),
        }
    }
}

#[glib::object_subclass]
impl ObjectSubclass for DotSeperator {
    const NAME: &'static str = "DotSeperator";

    type Type = super::DotSeperator;

    type ParentType = gtk::DrawingArea;
}

impl ObjectImpl for DotSeperator {
    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> = Lazy::new(|| {
            vec![
                ParamSpecDouble::builder("factor").build(),
                ParamSpecDouble::builder("spined").build(),
                ParamSpecDouble::builder("margin").build(),
                ParamSpecString::builder("color").build(),
            ]
        });
        PROPERTIES.as_ref()
    }

    fn set_property(&self, _id: usize, value: &Value, pspec: &ParamSpec) {
        match pspec.name() {
            "factor" => {
                let input_value = value.get().expect("The value needs to be of type `f64`.");
                self.factor.set(input_value);
            }
            "spined" => {
                let input_value = value.get().expect("The value needs to be of type `f64`.");
                self.spined.set(input_value);
            }
            "margin" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `f64`.");
                self.margin.set(input_value);
            }
            "color" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                let color = Color::from_hexcode(input_value);
                self.color.set((color.0, color.1, color.2));
            }
            _ => unimplemented!(),
        }
    }

    fn property(&self, _id: usize, pspec: &ParamSpec) -> Value {
        match pspec.name() {
            "factor" => self.factor.get().to_value(),
            "spined" => self.spined.get().to_value(),
            "margin" => self.margin.get().to_value(),
            "color" => {
                let color = self.color.get();
                Color::frgb_to_hexcode(color.0, color.1, color.2).to_value()
            }
            _ => unimplemented!(),
        }
    }
}

impl WidgetImpl for DotSeperator {}

impl DrawingAreaImpl for DotSeperator {}
