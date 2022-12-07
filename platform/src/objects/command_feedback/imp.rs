use std::cell::{Cell, RefCell};

use gtk::glib;
use gtk::subclass::prelude::*;

#[derive(Default)]
pub struct CommandFeedbackObject {
    pub command: RefCell<String>,
    pub param: RefCell<Option<String>>,
    pub comment: RefCell<String>,
    pub shortcuts: RefCell<Option<Vec<&'static str>>>,
    pub no_matching: Cell<bool>,
}

impl CommandFeedbackObject {
    pub fn set_command(&self, command: String) {
        *self.command.borrow_mut() = command;
    }

    pub fn set_param(&self, param: Option<String>) {
        *self.param.borrow_mut() = param;
    }

    pub fn set_comment(&self, comment: String) {
        *self.comment.borrow_mut() = comment;
    }

    pub fn set_shortcuts(&self, shortcuts: Option<Vec<&'static str>>) {
        *self.shortcuts.borrow_mut() = shortcuts;
    }

    pub fn set_no_matching(&self) {
        self.no_matching.set(true);
    }
}

#[glib::object_subclass]
impl ObjectSubclass for CommandFeedbackObject {
    const NAME: &'static str = "CommandFeedbackObject";

    type Type = super::CommandFeedbackObject;
}

impl ObjectImpl for CommandFeedbackObject {}
