use gtk::{subclass::prelude::*, glib};

#[derive(Default)]
pub struct CompositeLabel {}

#[glib::object_subclass]
impl ObjectSubclass for CompositeLabel {
    const NAME: &'static str = "CompositeLabel";

    type Type = super::CompositeLabel;

    type ParentType = gtk::Widget;
}

impl ObjectImpl for CompositeLabel {}

impl WidgetImpl for CompositeLabel {}