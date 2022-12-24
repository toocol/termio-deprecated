mod imp;

use gtk::{
    glib::{self, Object},
    subclass::prelude::*,
};
use libs::Color;

glib::wrapper! {
    pub struct ShortcutLabel(ObjectSubclass<imp::ShortcutLabel>)
        @extends gtk::DrawingArea, gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl ShortcutLabel {
    pub fn new(shortcuts: &[&'static str]) -> Self {
        let label:ShortcutLabel = Object::new(&[]);
        label.set_shortcuts(shortcuts.to_vec());
        label
    }

    pub fn from_properties(
        shortcuts: Vec<&'static str>,
        font_family: Option<String>,
        font_size: Option<i32>,
        hor_inset: Option<f64>,
        ver_inset: Option<f64>,
        join_inset: Option<f64>,
        radius: Option<f64>,
    ) -> Self {
        let mut font_family = font_family;
        let mut font_size = font_size;
        let mut hor_inset = hor_inset;
        let mut ver_inset = ver_inset;
        let mut join_inset = join_inset;
        let mut radius = radius;

        let mut builder = Object::builder();
        if let Some(font_family) = font_family.take() {
            builder = builder.property("font-family", font_family);
        }
        if let Some(font_size) = font_size.take() {
            builder = builder.property("font-size", font_size);
        }
        if let Some(hor_inset) = hor_inset.take() {
            builder = builder.property("hor-inset", hor_inset);
        }
        if let Some(ver_inset) = ver_inset.take() {
            builder = builder.property("ver-inset", ver_inset);
        }
        if let Some(join_inset) = join_inset.take() {
            builder = builder.property("join-inset", join_inset);
        }
        if let Some(radius) = radius.take() {
            builder = builder.property("radius", radius);
        }

        let shortcut_label: ShortcutLabel = builder.build();
        shortcut_label.set_shortcuts(shortcuts);
        shortcut_label.imp().update_draw_func();
        shortcut_label
    }

    pub fn set_shortcuts(&self, shortcuts: Vec<&'static str>) {
        *self.imp().shortcuts.borrow_mut() = shortcuts;
        self.imp().update_draw_func();
    }

    pub fn set_font_family(&self, font_faimly: String) {
        *self.imp().font_family.borrow_mut() = font_faimly;
        self.imp().update_draw_func();
    }

    pub fn set_font_size(&self, font_size: i32) {
        self.imp().font_size.set(font_size);
        self.imp().update_draw_func();
    }

    pub fn set_hor_inset(&self, hor_inset: f64) {
        self.imp().hor_inset.set(hor_inset);
        self.imp().update_draw_func();
    }

    pub fn set_ver_inset(&self, ver_inset: f64) {
        self.imp().ver_inset.set(ver_inset);
        self.imp().update_draw_func();
    }

    pub fn set_join_inset(&self, join_inset: f64) {
        self.imp().ver_inset.set(join_inset);
        self.imp().update_draw_func();
    }

    pub fn set_radius(&self, radius: f64) {
        self.imp().radius.set(radius);
        self.imp().update_draw_func();
    }

    pub fn set_line_width(&self, line_width: f64) {
        self.imp().line_width.set(line_width);
        self.imp().update_draw_func();
    }

    pub fn set_border_color(&self, r: u8, g: u8, b: u8) {
        let color = Color::from_rgb(r, g, b);
        self.imp().border_color.set(color);
        self.imp().update_draw_func();
    }

    pub fn set_background_color(&self, r: u8, g: u8, b: u8) {
        let color = Color::from_rgb(r, g, b);
        self.imp().background_color.set(color);
        self.imp().update_draw_func();
    }

    pub fn set_font_color(&self, hexcode: &str) {
        *self.imp().font_color.borrow_mut() = hexcode.to_string();
        self.imp().update_draw_func();
    }
}
