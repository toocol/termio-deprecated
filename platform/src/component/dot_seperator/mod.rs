mod imp;

use gtk::{
    glib::{self, Object},
    prelude::ToValue,
    subclass::prelude::*,
};
use libs::Color;

glib::wrapper! {
    pub struct DotSeperator(ObjectSubclass<imp::DotSeperator>)
        @extends gtk::DrawingArea, gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl DotSeperator {
    pub fn new() -> Self {
        let seperator: DotSeperator = Object::new(&[]);
        seperator.imp().update_draw_func();
        seperator
    }

    pub fn builder() -> DotSeperatorBuilder {
        DotSeperatorBuilder::new()
    }

    pub fn set_factor(&self, factor: f64) {
        self.imp().factor.set(factor);
        self.imp().update_draw_func();
    }

    pub fn set_spined(&self, spined: f64) {
        self.imp().spined.set(spined);
        self.imp().update_draw_func();
    }

    pub fn set_margin(&self, margin: f64) {
        self.imp().spined.set(margin);
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
}

#[derive(Default)]
pub struct DotSeperatorBuilder {
    factor: Option<f64>,
    spined: Option<f64>,
    margin: Option<f64>,
    color: Option<String>,
}

impl DotSeperatorBuilder {
    pub fn new() -> Self {
        Self::default()
    }

    pub fn build(self) -> DotSeperator {
        let mut properties: Vec<(&str, &dyn ToValue)> = vec![];

        if let Some(ref factor) = self.factor {
            properties.push(("factor", factor))
        }
        if let Some(ref spined) = self.spined {
            properties.push(("spined", spined))
        }
        if let Some(ref margin) = self.margin {
            properties.push(("margin", margin))
        }
        if let Some(ref color) = self.color {
            properties.push(("color", color))
        }

        let seperator: DotSeperator = Object::new(&properties);
        seperator.imp().update_draw_func();
        seperator
    }

    pub fn factor(mut self, factor: f64) -> Self {
        self.factor = Some(factor);
        self
    }

    pub fn spined(mut self, spined: f64) -> Self {
        self.spined = Some(spined);
        self
    }

    pub fn margin(mut self, margin: f64) -> Self {
        self.margin = Some(margin);
        self
    }

    pub fn color(mut self, color: &str) -> Self {
        self.color = Some(color.to_string());
        self
    }
}
