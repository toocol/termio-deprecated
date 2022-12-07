mod imp;

use gtk::{
    glib::{self, Object},
    subclass::prelude::ObjectSubclassIsExt,
};

use crate::CommandFeedbackObject;

glib::wrapper! {
    pub struct CommandFeedbackItem(ObjectSubclass<imp::CommandFeedbackItem>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl CommandFeedbackItem {
    pub fn from_object(feedback: &CommandFeedbackObject) -> Self {
        let item = CommandFeedbackItem::new();
        let imp = item.imp();

        if feedback.is_no_matching() {
            let label = feedback.no_matching();
            imp.set_no_matching(label);
        } else {
            let command = feedback.command();
            imp.set_command(command);

            if let Some(param) = feedback.param() {
                imp.set_param(param);
            }

            let comment = feedback.comment();
            imp.set_comment(comment);

            if let Some(shortcuts) = feedback.shortcuts() {
                imp.set_shortcuts(shortcuts);
            }
        }

        item
    }

    fn new() -> Self {
        Object::new(&[])
    }
}
