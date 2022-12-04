use std::f64::consts::PI;

use gtk::{
    glib::{self, clone},
    prelude::*,
    subclass::prelude::*,
};

#[derive(Default)]
pub struct DotSeperator {}

#[glib::object_subclass]
impl ObjectSubclass for DotSeperator {
    const NAME: &'static str = "DotSeperator";

    type Type = super::DotSeperator;

    type ParentType = gtk::DrawingArea;
}

impl ObjectImpl for DotSeperator {
    fn constructed(&self) {
        self.parent_constructed();

        self.instance().set_draw_func(
            clone!(@weak self as seperator => move |_, cr, width, height| {
            let width = width as f64;
            let height = height as f64;

            let single = width / 2. * 0.8;
            let spined = 1.5;
            let margin = 15.;
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

impl WidgetImpl for DotSeperator {}

impl DrawingAreaImpl for DotSeperator {}
