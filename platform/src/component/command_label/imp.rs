use std::cell::RefCell;

use gtk::glib::once_cell::sync::OnceCell;
use gtk::subclass::prelude::*;
use gtk::traits::WidgetExt;
use gtk::{glib, Label, Orientation};

use crate::{PartialSeperator, SemanticMarkupBuilder};

/// `CommandLabel` was used on command panel feedback row to display command label with
/// matching text highlight and with a PartialSeperator on it's right.
#[derive(Default)]
pub struct CommandLabel {
    pub command: RefCell<String>,
    pub matching: RefCell<String>,
    pub label: OnceCell<Label>,
    pub seperator: OnceCell<PartialSeperator>,
}

impl CommandLabel {
    pub fn set_command_matching(&self, command: String, matching: String) {
        self.label
            .get()
            .expect("`label` of `CommandPanel` is None.")
            .set_markup(&SemanticMarkupBuilder::parse_markup(
                command.as_str(),
                matching.as_str(),
            ));

        *self.command.borrow_mut() = command;
        *self.matching.borrow_mut() = matching;
    }
}

#[glib::object_subclass]
impl ObjectSubclass for CommandLabel {
    const NAME: &'static str = "MatchingLabel";

    type Type = super::MatchingLabel;

    type ParentType = gtk::Widget;

    fn class_init(klass: &mut Self::Class) {
        klass.set_layout_manager_type::<gtk::BoxLayout>();
    }
}

impl ObjectImpl for CommandLabel {
    fn constructed(&self) {
        self.parent_constructed();

        let label = Label::new(None);
        label.set_use_markup(true);
        label.set_parent(&*self.instance());
        self.label
            .set(label)
            .expect("`label` of `CommandLabel` is None.");

        let seperator = PartialSeperator::new(Orientation::Vertical);
        seperator.set_parent(&*self.instance());
        self.seperator
            .set(seperator)
            .expect("`seperator` of `CommandLabel` is None.");
    }

    fn dispose(&self) {
        if let Some(label) = self.label.get() {
            label.unparent();
        }
        if let Some(seperator) = self.seperator.get() {
            seperator.unparent();
        }
    }
}

impl WidgetImpl for CommandLabel {}
