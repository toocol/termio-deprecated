mod imp;

use std::cell::Cell;

use gtk::{
    glib::{self, Object},
    prelude::IsA,
    subclass::prelude::ObjectSubclassIsExt,
    traits::{EditableExt, WidgetExt},
    EventControllerKey, Inhibit, Widget,
};
use utilities::TimeStamp;

use crate::{
    ACTION_TOGGLE_COMMAND_PANEL, GTK_KEYCODE_ESCAPE, GTK_KEYCODE_SHIFT_L, GTK_KEYCODE_SHIFT_R,
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
