#![allow(dead_code)]
use std::{
    cell::{Cell, RefCell},
    f64::consts::PI,
    rc::Rc,
};

use gtk::{
    glib::{
        self, once_cell::sync::Lazy, ParamSpec, ParamSpecDouble, ParamSpecInt, ParamSpecString,
        Value,
    },
    prelude::*,
    subclass::prelude::*,
};
use utilities::Color;

const DEFAULT_FONT_SIZE: i32 = 10;
const DEFAULT_HOR_INSET: f64 = 5.;
const DEFAULT_VER_INSET: f64 = 3.;
const DEFAULT_JOIN_INSET: f64 = 3.;
const DEFAULT_RADUIS: f64 = 5.;
const DEFAULT_LINE_WIDTH: f64 = 0.5;
const DEFAULT_BORDER_COLOR: (f64, f64, f64) = (0.4, 0.4, 0.4);
const DEFAULT_BACKGROUND_COLOR: (f64, f64, f64) = (1., 1., 1.);
const DEFAULT_FONT_COLOR: &'static str = "black";

pub struct ShortcutLabel {
    pub shortcuts: RefCell<Vec<&'static str>>,
    pub font_family: RefCell<String>,
    pub font_size: Cell<i32>,
    pub hor_inset: Cell<f64>,
    pub ver_inset: Cell<f64>,
    pub join_inset: Cell<f64>,
    pub radius: Cell<f64>,

    pub line_width: Cell<f64>,
    pub border_color: Cell<(f64, f64, f64)>,
    pub background_color: Cell<(f64, f64, f64)>,
    pub font_color: RefCell<String>,
}

impl Default for ShortcutLabel {
    fn default() -> Self {
        Self {
            shortcuts: Default::default(),
            font_family: Default::default(),
            font_size: Cell::new(DEFAULT_FONT_SIZE),
            hor_inset: Cell::new(DEFAULT_HOR_INSET),
            ver_inset: Cell::new(DEFAULT_VER_INSET),
            join_inset: Cell::new(DEFAULT_JOIN_INSET),
            radius: Cell::new(DEFAULT_RADUIS),
            line_width: Cell::new(DEFAULT_LINE_WIDTH),
            border_color: Cell::new(DEFAULT_BORDER_COLOR),
            background_color: Cell::new(DEFAULT_BACKGROUND_COLOR),
            font_color: RefCell::new(DEFAULT_FONT_COLOR.to_string()),
        }
    }
}

#[glib::object_subclass]
impl ObjectSubclass for ShortcutLabel {
    const NAME: &'static str = "ShortcutLabel";

    type Type = super::ShortcutLabel;

    type ParentType = gtk::DrawingArea;
}

impl ShortcutLabel {
    pub fn update_draw_func(&self) {
        let shortcuts = self.shortcuts.borrow().clone();
        let font_family = self.font_family.borrow().clone();
        let font_size = self.font_size.get();
        let hor_inset = self.hor_inset.get();
        let ver_inset = self.ver_inset.get();
        let join_inset = self.join_inset.get();
        let radius = self.radius.get();
        let line_width = self.line_width.get();
        let border_color = self.border_color.get();
        let background_color = self.background_color.get();
        let font_color = self.font_color.borrow().clone();

        let obj = self.instance();

        let layouts = Rc::new(RefCell::new(vec![]));
        let mut size = (0, 0);

        for i in 0..shortcuts.len() {
            let key = shortcuts[i];
            let layout = obj.create_pango_layout(None);
            layout.set_markup(
                format!(
                    "<span foreground=\"{}\" font_desc=\"{} {}\">{}</span>",
                    font_color, font_family, font_size, key
                )
                .as_str(),
            );
            let pixel_size = layout.pixel_size();
            size.0 += pixel_size.0 + hor_inset as i32 * 2;
            size.1 = size.1.max(pixel_size.1 + ver_inset as i32 * 2);
            layouts.borrow_mut().push(layout);

            if i != shortcuts.len() - 1 {
                let layout = obj.create_pango_layout(None);
                layout.set_markup(
                    format!(
                        "<span foreground=\"{}\" font_desc=\"{} {}\">+</span>",
                        Color::frgb_to_hexcode(border_color.0, border_color.1, border_color.2),
                        font_family,
                        font_size
                    )
                    .as_str(),
                );
                let pixel_size = layout.pixel_size();
                size.0 += pixel_size.0 + join_inset as i32 * 2;
                size.1 = size.1.max(pixel_size.1 + ver_inset as i32 * 2);
                layouts.borrow_mut().push(layout);
            }
        }

        obj.set_content_width(size.0);
        obj.set_content_height(size.1);
        let layouts_clone = layouts.clone();

        obj.set_draw_func(move |_, cr, _, _| {
            let mut current_rec_x = 0.;
            let mut current_text_x = hor_inset;
            let degress = PI / 180.;

            cr.set_line_width(line_width);

            for i in 0..layouts_clone.borrow().len() {
                let layout = &layouts_clone.borrow()[i];
                let pixel_size = layout.pixel_size();

                if i % 2 == 0 {
                    let width = pixel_size.0 as f64 + hor_inset * 2.;
                    let height = pixel_size.1 as f64 + ver_inset;
                    // Draw the rectangle with round corner.
                    cr.new_sub_path();
                    cr.arc(
                        current_rec_x + width - radius,
                        0. + radius,
                        radius,
                        -90. * degress,
                        0. * degress,
                    );
                    cr.arc(
                        current_rec_x + width - radius,
                        0. + height - radius,
                        radius,
                        0. * degress,
                        90. * degress,
                    );
                    cr.arc(
                        current_rec_x + radius,
                        0. + height - radius,
                        radius,
                        90. * degress,
                        180. * degress,
                    );
                    cr.arc(
                        current_rec_x + radius,
                        0. + radius,
                        radius,
                        180. * degress,
                        270. * degress,
                    );
                    cr.close_path();

                    cr.set_source_rgb(background_color.0, background_color.1, background_color.2);
                    cr.fill_preserve().unwrap();
                    cr.set_source_rgb(border_color.0, border_color.1, border_color.2);
                    cr.stroke().unwrap();

                    cr.move_to(current_text_x, ver_inset);
                    pangocairo::show_layout(cr, layout);
                    current_text_x += pixel_size.0 as f64 + hor_inset;
                    current_rec_x += pixel_size.0 as f64 + hor_inset * 2.;
                } else {
                    cr.move_to(current_text_x + join_inset, ver_inset);
                    pangocairo::show_layout(cr, layout);
                    current_text_x += pixel_size.0 as f64 + hor_inset + join_inset * 2.;
                    current_rec_x += pixel_size.0 as f64 + join_inset * 2.;
                }
            }
        });
    }
}

impl ObjectImpl for ShortcutLabel {
    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> = Lazy::new(|| {
            vec![
                ParamSpecString::builder("font-family").build(),
                ParamSpecInt::builder("font-size").build(),
                ParamSpecDouble::builder("hor-inset").build(),
                ParamSpecDouble::builder("ver-inset").build(),
                ParamSpecDouble::builder("join-inset").build(),
                ParamSpecDouble::builder("radius").build(),
            ]
        });
        PROPERTIES.as_ref()
    }

    fn set_property(&self, _id: usize, value: &Value, pspec: &ParamSpec) {
        match pspec.name() {
            "font-family" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                *self.font_family.borrow_mut() = input_value;
            }
            "font-size" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.font_size.set(input_value);
            }
            "hor-inset" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.hor_inset.set(input_value);
            }
            "ver-inset" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.ver_inset.set(input_value);
            }
            "join-inset" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.join_inset.set(input_value);
            }
            "radius" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.radius.set(input_value);
            }
            _ => unimplemented!(),
        }
    }

    fn property(&self, _id: usize, pspec: &ParamSpec) -> Value {
        match pspec.name() {
            "font-family" => self.font_family.borrow().to_value(),
            "font-size" => self.font_size.get().to_value(),
            "hor-inset" => self.hor_inset.get().to_value(),
            "ver-inset" => self.ver_inset.get().to_value(),
            "join-inset" => self.join_inset.get().to_value(),
            "radius" => self.radius.get().to_value(),
            _ => unimplemented!(),
        }
    }
}

impl WidgetImpl for ShortcutLabel {}

impl DrawingAreaImpl for ShortcutLabel {}
