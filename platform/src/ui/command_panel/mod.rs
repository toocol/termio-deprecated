mod imp;

use std::cell::Cell;

use gtk::{
    gio::ListStore,
    glib::{self, clone, Object},
    prelude::*,
    subclass::prelude::*,
    traits::{EditableExt, WidgetExt},
    EventControllerKey, Inhibit, ListBoxRow, Widget,
};
use utilities::TimeStamp;

use crate::{
    CommandFeedbackItem, CommandFeedbackObject, ACTION_TOGGLE_COMMAND_PANEL, GTK_KEYCODE_ESCAPE,
    GTK_KEYCODE_SHIFT_L, GTK_KEYCODE_SHIFT_R,
};

glib::wrapper! {
    pub struct CommandPanel(ObjectSubclass<imp::CommandPanel>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl CommandPanel {
    pub fn new() -> Self {
        Object::new(&[])
    }

    pub fn setup_collections(&self) {
        let collections = ListStore::new(CommandFeedbackObject::static_type());
        self.imp()
            .collections
            .set(collections.clone())
            .expect("Could not set collections.");

        self.imp()
            .feedbacks
            .get()
            .expect("`feedbacks` of CommandPanel is None")
            .bind_model(
                Some(&collections),
                clone!(@weak self as panel => @default-panic, move |obj| {
                    let command_feedback = obj
                        .downcast_ref()
                        .expect("The object should be of type `CollectionObject`.");
                    let row = panel.create_feedback_row(command_feedback);
                    row.upcast()
                }),
            );
    }

    pub fn setup_callbacks(&self) {
        //// Key events
        let key_controller = EventControllerKey::new();
        let widget = self.clone();
        key_controller.connect_key_pressed(move |_, _, keycode, _| {
            widget.imp().shortcut_watcher.esc(&widget, keycode);
            widget.imp().shortcut_watcher.watch(&widget, keycode);
            Inhibit(false)
        });
        self.add_controller(&key_controller);

        //// Entry text change
        let entry = self
            .imp()
            .entry
            .get()
            .expect("`entry` of CommandPanel is None.");
        entry.connect_text_notify(clone!(@weak self as panel => move |entry| {
            panel.clear_collections();

            let feedbacks = panel
                .imp()
                .dynamic_feedback
                .dynamic_feedback(entry.text().as_str());

            if feedbacks.len() > 0 {
                let feedbacks: Vec<CommandFeedbackObject> = feedbacks.into_iter()
                    .map(CommandFeedbackObject::from_command_feedback)
                    .collect();
                for feedback in feedbacks.into_iter() {
                    panel.add_to_collections(feedback);
                }
            } else {
                let feedback = CommandFeedbackObject::no_matching_command();
                panel.add_to_collections(feedback);
            }
        }));
    }

    pub fn add_to_collections(&self, feedback: CommandFeedbackObject) {
        self.imp()
            .collections
            .get()
            .expect("`collections` of `CommandFeedbackObject` is None.")
            .append(&feedback);
    }

    pub fn clear_collections(&self) {
        self.imp()
            .collections
            .get()
            .expect("`collections` of `CommandFeedbackObject` is None.")
            .remove_all();
    }

    pub fn create_feedback_row(&self, command_feedback: &CommandFeedbackObject) -> ListBoxRow {
        let command_feedback_item = CommandFeedbackItem::from_object(command_feedback);
        ListBoxRow::builder().child(&command_feedback_item).build()
    }

    pub fn entry_grab_focus(&self) {
        self.imp()
            .entry
            .get()
            .expect("`entry` of `CommandPanel` is None")
            .grab_focus();
    }

    pub fn clear_entry(&self) {
        self.imp()
            .entry
            .get()
            .expect("`entry` of `CommandPanel` is None.")
            .set_text("");
    }
}

#[derive(Debug, Default)]
pub struct ShortcutWatcher {
    pub shift_pressed: Cell<bool>,
    pub shift_timestamp: Cell<u64>,
}

impl ShortcutWatcher {
    pub fn watch<T: IsA<Widget>>(&self, widget: &T, keycode: u32) {
        if keycode == GTK_KEYCODE_SHIFT_L || keycode == GTK_KEYCODE_SHIFT_R {
            if self.shift_pressed.get() {
                if TimeStamp::timestamp() - self.shift_timestamp.get() < 1000 {
                    widget
                        .activate_action(&ACTION_TOGGLE_COMMAND_PANEL.activate(), None)
                        .expect(
                            format!(
                                "Activate action `{}` failed",
                                ACTION_TOGGLE_COMMAND_PANEL.activate()
                            )
                            .as_str(),
                        );
                }
                self.shift_pressed.set(false);
            } else {
                self.shift_pressed.set(true);
                self.shift_timestamp.set(TimeStamp::timestamp());
            }
        } else {
            self.shift_pressed.set(false);
        }
    }

    pub fn esc<T: IsA<Widget>>(&self, widget: &T, keycode: u32) {
        if keycode == GTK_KEYCODE_ESCAPE {
            widget
                .activate_action(&ACTION_TOGGLE_COMMAND_PANEL.activate(), None)
                .expect(
                    format!(
                        "Activate action `{}` failed",
                        ACTION_TOGGLE_COMMAND_PANEL.activate()
                    )
                    .as_str(),
                );
        }
    }
}
