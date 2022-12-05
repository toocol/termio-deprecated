mod imp;

use gtk::glib::{self, Object};

use crate::CommandFeedbackObject;

glib::wrapper! {
    pub struct CommandFeedbackItem(ObjectSubclass<imp::CommandFeedbackItem>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl CommandFeedbackItem {
    pub fn new() -> Self {
        Object::new(&[])
    }

    pub fn from_object(_command_feedback: &CommandFeedbackObject) -> Self {
        todo!()
    }
}