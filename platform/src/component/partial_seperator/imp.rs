use std::cell::Cell;

use gtk::{
    glib::{self, once_cell::sync::Lazy, ParamSpec, ParamSpecDouble, ParamSpecString, Value},
    prelude::*,
    subclass::prelude::*,
    Orientation,
};
use libs::Color;

const DEFAULT_LINE_WIDTH: f64 = 0.5;
const DEFAULT_FACTOR: f64 = 0.8;
const DEFAULT_COLOR: (f64, f64, f64) = (0.2, 0.2, 0.2);

pub struct PartialSeperator {
    pub line_width: Cell<f64>,
    pub factor: Cell<f64>,
    pub color: Cell<(f64, f64, f64)>,
    pub orientation: Cell<Orientation>,
}

impl Default for PartialSeperator {
    fn default() -> Self {
        Self {
            line_width: Cell::new(DEFAULT_LINE_WIDTH),
            factor: Cell::new(DEFAULT_FACTOR),
            color: Cell::new(DEFAULT_COLOR),
            orientation: Cell::new(Orientation::Vertical),
        }
    }
}

impl PartialSeperator {
    pub fn update_draw_func(&self) {
        let line_width = self.line_width.get();
        let factor = self.factor.get();
        let color = self.color.get();
        let orientation = self.orientation.get();

        self.instance().set_draw_func(move |_, cr, width, height| {
            cr.set_line_width(line_width);
            cr.set_source_rgb(color.0, color.1, color.2);

            match orientation {
                Orientation::Horizontal => {
                    let start = width as f64 * (1. - factor) / 2.;
                    let line_width = width as f64 * factor;
                    cr.move_to(start, 0.);
                    cr.line_to(line_width, 0.);
                    cr.stroke().unwrap();
                },
                Orientation::Vertical => {
                    let start = height as f64 * (1. - factor) / 2.;
                    let line_height = height as f64 * factor;
                    cr.move_to(0., start);
                    cr.line_to(0., line_height);
                    cr.stroke().unwrap();
                },
                _ => unimplemented!()
            }
        });
    }
}

#[glib::object_subclass]
impl ObjectSubclass for PartialSeperator {
    const NAME: &'static str = "PartialSeperator";

    type Type = super::PartialSeperator;

    type ParentType = gtk::DrawingArea;
}

impl ObjectImpl for PartialSeperator {
    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> = Lazy::new(|| {
            vec![
                ParamSpecDouble::builder("line-width").build(),
                ParamSpecDouble::builder("factor").build(),
                ParamSpecString::builder("color").build(),
                ParamSpecString::builder("orientation").build(),
            ]
        });
        PROPERTIES.as_ref()
    }

    fn set_property(&self, _id: usize, value: &Value, pspec: &ParamSpec) {
        match pspec.name() {
            "line-width" => {
                let input_value = value.get().expect("The value needs to be of type `f64`.");
                self.line_width.set(input_value);
            }
            "factor" => {
                let input_value = value.get().expect("The value needs to be of type `f64`.");
                self.factor.set(input_value);
            }
            "color" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                let color = Color::from_hexcode(input_value);
                self.color.set((color.0, color.1, color.2));
            }
            "orientation" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.orientation.set(input_value);
            }
            _ => unimplemented!(),
        }
    }

    fn property(&self, _id: usize, pspec: &ParamSpec) -> Value {
        match pspec.name() {
            "line-width" => self.line_width.get().to_value(),
            "factor" => self.factor.get().to_value(),
            "color" => {
                let color = self.color.get();
                Color::frgb_to_hexcode(color.0, color.1, color.2).to_value()
            }
            "orientation" => self.orientation.get().to_value(),
            _ => unimplemented!(),
        }
    }
}

impl WidgetImpl for PartialSeperator {}

impl DrawingAreaImpl for PartialSeperator {}
