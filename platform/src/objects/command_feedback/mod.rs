mod imp;

use core::CommandFeedback;

use gtk::glib;

glib::wrapper! {
    pub struct CommandFeedbackObject(ObjectSubclass<imp::CommandFeedbackObject>);
}

impl CommandFeedbackObject {
    pub fn from_command_feedback(_feedback: &CommandFeedback) -> Self {
        todo!()
    }
}