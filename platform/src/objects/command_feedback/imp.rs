use gtk::glib;
use gtk::subclass::prelude::*;

#[derive(Default)]
pub struct CommandFeedbackObject {}

#[glib::object_subclass]
impl ObjectSubclass for CommandFeedbackObject {
    const NAME: &'static str = "CommandFeedbackObject";

    type Type = super::CommandFeedbackObject;
}

impl ObjectImpl for CommandFeedbackObject {}
