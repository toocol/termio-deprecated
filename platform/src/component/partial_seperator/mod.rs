mod imp;

use gtk::{
    glib::{self, Object},
    prelude::ToValue,
    subclass::prelude::*,
    Orientation,
};
use utilities::Color;

glib::wrapper! {
    pub struct PartialSeperator(ObjectSubclass<imp::PartialSeperator>)
        @extends gtk::DrawingArea, gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl PartialSeperator {
    pub fn new(orientation: Orientation) -> Self {
        let seperator: PartialSeperator = Object::new(&[("orientation", &orientation)]);
        seperator.imp().update_draw_func();
        seperator
    }

    pub fn builder() -> PartialSeperatorBuilder {
        PartialSeperatorBuilder::new()
    }

    pub fn set_line_width(&self, line_width: f64) {
        self.imp().line_width.set(line_width);
        self.imp().update_draw_func();
    }

    pub fn set_factor(&self, factor: f64) {
        self.imp().factor.set(factor);
        self.imp().update_draw_func();
    }

    pub fn set_color_hex(&self, hex_color: &str) {
        let color = Color::from_hexcode(hex_color);
        self.imp().color.set((color.0, color.1, color.2));
        self.imp().update_draw_func();
    }

    pub fn set_color_rgb(&self, r: u8, g: u8, b: u8) {
        self.imp().color.set(Color::from_rgb(r, g, b));
        self.imp().update_draw_func();
    }

    pub fn set_orientation(&self, orientation: Orientation) {
        self.imp().orientation.set(orientation);
        self.imp().update_draw_func();
    }
}

#[derive(Default)]
pub struct PartialSeperatorBuilder {
    line_width: Option<f64>,
    factor: Option<f64>,
    color: Option<String>,
    orientation: Option<Orientation>,
}

impl PartialSeperatorBuilder {
    pub fn new() -> Self {
        Self::default()
    }

    pub fn build(self) -> PartialSeperator {
        let mut properties: Vec<(&str, &dyn ToValue)> = vec![];

        if let Some(ref line_width) = self.factor {
            properties.push(("line-width", line_width));
        }
        if let Some(ref factor) = self.factor {
            properties.push(("factor", factor))
        }
        if let Some(ref color) = self.color {
            properties.push(("color", color))
        }
        if let Some(ref orientation) = self.orientation {
            properties.push(("orientation", orientation))
        }

        let seperator: PartialSeperator = Object::new(&properties);
        seperator.imp().update_draw_func();
        seperator
    }

    pub fn line_width(mut self, line_width: f64) -> Self {
        self.line_width = Some(line_width);
        self
    }

    pub fn factor(mut self, factor: f64) -> Self {
        self.factor = Some(factor);
        self
    }

    pub fn color(mut self, color: &str) -> Self {
        self.color = Some(color.to_string());
        self
    }

    pub fn orientation(mut self, orientation: Orientation) -> Self {
        self.orientation = Some(orientation);
        self
    }
}
