mod imp;

use gtk::gio::SimpleAction;
use gtk::glib::{Object, clone};
use gtk::{prelude::*, Dialog, DialogFlags, ResponseType, Entry};
use gtk::subclass::prelude::ObjectSubclassIsExt;
use gtk::{gio, glib, Application};

glib::wrapper! {
    pub struct TermioCommunityWindow(ObjectSubclass<imp::TermioCommunityWindow>)
        @extends gtk::ApplicationWindow, gtk::Window, gtk::Widget,
        @implements gio::ActionGroup, gio::ActionMap, gtk::Accessible, gtk::Buildable,
                    gtk::ConstraintTarget, gtk::Native, gtk::Root, gtk::ShortcutManager;
}

impl TermioCommunityWindow {
    pub fn new(app: &Application) -> Self {
        Object::new(&[("application", app)])
    }

    pub fn initialize(&self) {
        self.imp().native_terminal_emulator.initialize();
    }

    pub fn setup_actions(&self) {
        // Create `new-session-credential` action.
        let action_new_session_credential = SimpleAction::new("new-session-credential", None);
        action_new_session_credential.connect_activate(clone!(@weak self as scmt => move |_, _| {
            scmt.call_new_session_credential_dialog();
        }));
        self.add_action(&action_new_session_credential);
    }

    fn call_new_session_credential_dialog(&self) {
        // Create new Dialog
        let dialog = Dialog::with_buttons(
            Some("New Collection"),
            Some(self),
            DialogFlags::MODAL
                | DialogFlags::DESTROY_WITH_PARENT
                | DialogFlags::USE_HEADER_BAR,
            &[
                ("Cancel", ResponseType::Cancel),
                ("Create", ResponseType::Accept),
            ],
        );
        dialog.set_default_response(ResponseType::Accept);
        // dialog.add_css_class("add-collection-dialog");

        // Make the dialog button insensitive initially, and add css class
        let dialog_button = dialog
            .widget_for_response(ResponseType::Accept)
            .expect("The dialog needs to have a widget for response type `Accept`.");
        dialog_button.set_sensitive(false);
        dialog_button.set_css_classes(&["add-collection-dialog", "collection-accept"]);

        let cancel_button = dialog
            .widget_for_response(ResponseType::Cancel)
            .expect("The dialog needs to have a widget for response type `Accept`.");
        cancel_button.set_css_classes(&["add-collection-dialog"]);

        // Create entry and add it to the dialog
        let entry = Entry::builder()
            .margin_top(12)
            .margin_bottom(12)
            .margin_start(12)
            .margin_end(12)
            .placeholder_text("Name")
            .activates_default(true)
            .build();
        dialog.content_area().append(&entry);

        // Set entry's css class to "error", when there is not text in it
        entry.connect_changed(clone!(@weak dialog => move |entry| {
            let text = entry.text();
            let dialog_button = dialog.
                widget_for_response(ResponseType::Accept).
                expect("The dialog needs to have a widget for response type `Accept`.");
            let empty = text.is_empty();

            dialog_button.set_sensitive(!empty);

            if empty {
                entry.add_css_class("error");
            } else {
                entry.remove_css_class("error");
            }
        }));
        dialog.present();
    }
}
