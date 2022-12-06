mod imp;

use core::CommandFeedback;

use gtk::glib::Object;
use gtk::subclass::prelude::*;
use gtk::{glib, Label};
use utilities::DynamicBundle;

use crate::{LanguageBundle, ShortcutLabel};

glib::wrapper! {
    pub struct CommandFeedbackObject(ObjectSubclass<imp::CommandFeedbackObject>);
}

impl CommandFeedbackObject {
    pub fn from_command_feedback(feedback: CommandFeedback) -> Self {
        let command_feed_back_obj: CommandFeedbackObject = Object::new(&[]);
        let imp = command_feed_back_obj.imp();
        imp.set_command(feedback.command);
        imp.set_param(feedback.param);
        imp.set_comment(feedback.comment);
        imp.set_shortcuts(feedback.shortcuts);
        command_feed_back_obj
    }

    pub fn command(&self) -> Label {
        Label::new(Some(self.imp().command.borrow().as_ref()))
    }

    pub fn param(&self) -> Option<Label> {
        if let Some(param) = self.imp().param.borrow().as_deref() {
            Some(Label::new(Some(param)))
        } else {
            None
        }
    }

    pub fn comment(&self) -> Label {
        let comment = LanguageBundle::message(self.imp().comment.borrow().as_ref(), None);
        Label::new(Some(&comment))
    }

    pub fn shortcuts(&self) -> Option<ShortcutLabel> {
        if let Some(shortcuts) = self.imp().shortcuts.borrow().as_ref() {
            let label = ShortcutLabel::new(&shortcuts);
            Some(label)
        } else {
            None
        }
    }
}
