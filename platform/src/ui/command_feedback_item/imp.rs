use std::cell::RefCell;

use crate::ShortcutLabel;
use gtk::{
    glib::{self, once_cell::sync::OnceCell},
    prelude::*,
    subclass::prelude::*,
    Label, Align,
};

#[derive(Default)]
pub struct CommandFeedbackItem {
    left_box: RefCell<gtk::Box>,
    right_box: RefCell<gtk::Box>,

    pub no_matching_label: OnceCell<Label>,
    pub command: OnceCell<Label>,
    pub comment: OnceCell<Label>,
    pub param: OnceCell<Label>,
    pub shortcuts: OnceCell<ShortcutLabel>,
}

impl CommandFeedbackItem {
    pub fn set_no_matching(&self, label: Label) {
        self.left_box.borrow().append(&label);
        self.no_matching_label
            .set(label)
            .expect("`no_matching_label` of `CommandFeedbackItem` can only set once.");
    }

    pub fn set_command(&self, label: Label) {
        self.left_box.borrow().append(&label);
        self.command
            .set(label)
            .expect("`command` of `CommandFeedbackItem` can only set once.");
    }

    pub fn set_comment(&self, label: Label) {
        self.left_box.borrow().append(&label);
        self.comment
            .set(label)
            .expect("`comment` of `CommandFeedbackItem` can only set once.");
    }

    pub fn set_param(&self, label: Label) {
        self.left_box.borrow().append(&label);
        self.param
            .set(label)
            .expect("`param` of `CommandFeedbackItem` can only set once.");
    }

    pub fn set_shortcuts(&self, label: ShortcutLabel) {
        self.right_box.borrow().append(&label);
        self.shortcuts
            .set(label)
            .expect("`shortcuts` of `CommandFeedbackItem` can only set once.");
    }
}

#[glib::object_subclass]
impl ObjectSubclass for CommandFeedbackItem {
    const NAME: &'static str = "CommandFeedbackItem";

    type Type = super::CommandFeedbackItem;

    type ParentType = gtk::Widget;

    fn class_init(klass: &mut Self::Class) {
        klass.set_layout_manager_type::<gtk::BoxLayout>();
    }
}

impl ObjectImpl for CommandFeedbackItem {
    fn constructed(&self) {
        self.parent_constructed();
        let obj = self.instance();
        obj.add_css_class("command-feedback-item");
        obj.set_hexpand(true);

        let layout = self
            .instance()
            .layout_manager()
            .unwrap()
            .downcast::<gtk::BoxLayout>()
            .unwrap();
        layout.set_orientation(gtk::Orientation::Horizontal);
        layout.set_spacing(0);

        let left_box = self.left_box.borrow();
        let right_box = self.right_box.borrow();

        left_box.set_parent(&*obj);
        right_box.set_parent(&*obj);

        left_box.add_css_class("left-box");
        right_box.add_css_class("right-box");

        left_box.set_halign(Align::Start);
        right_box.set_halign(Align::End);

        left_box.set_orientation(gtk::Orientation::Horizontal);
        right_box.set_orientation(gtk::Orientation::Horizontal);

        left_box.set_hexpand(true);
        right_box.set_hexpand(true);

        left_box.set_spacing(10);
        right_box.set_spacing(10);

        left_box.set_margin_start(5);
        right_box.set_margin_end(5);
    }

    fn dispose(&self) {
        if let Some(no_matching) = self.no_matching_label.get() {
            no_matching.unparent();
        }
        if let Some(command) = self.command.get() {
            command.unparent();
        }
        if let Some(comment) = self.comment.get() {
            comment.unparent();
        }
        if let Some(param) = self.param.get() {
            param.unparent();
        }
        if let Some(shortcuts) = self.shortcuts.get() {
            shortcuts.unparent();
        }
        self.left_box.borrow().unparent();
        self.right_box.borrow().unparent();
    }
}

impl WidgetImpl for CommandFeedbackItem {}
