mod imp;

use core::SessionCredential;
use std::fs::File;

use gtk::{
    gio::{self, SimpleAction},
    glib::{self, clone, Object},
    prelude::*,
    subclass::prelude::ObjectSubclassIsExt,
    Application,
};

use crate::util::data_path;

use super::NewSessionDialog;

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
        self.imp()
            .new_session_dialog
            .set(NewSessionDialog::new(self))
            .expect("`new_session_dialog` of `TermioCommunityWindow` can only set once.");
    }

    pub fn setup_actions(&self) {
        // Create `new-session-credential` action.
        let action_new_session_credential = SimpleAction::new("new-session-credential", None);
        action_new_session_credential.connect_activate(clone!(@weak self as window => move |_, _| {
            window.imp()
                .new_session_dialog
                .get()
                .expect("`new_session_dialog` of `TermioCommunityWindow` must initialized first before use.")
                .show_dialog();
        }));
        self.add_action(&action_new_session_credential);

        let action_hide_left_side_bar = SimpleAction::new("hide-left-side-bar", None);
        action_hide_left_side_bar.connect_activate(clone!(@weak self as window => move |_, _| {
            window.imp().workspace_left_side_bar.hide();
            let allocation = window.imp()
                .workspace_terminal_scrolled_window
                .allocation();
            window.imp()
                .native_terminal_emulator
                .resize(allocation.width(), allocation.height());
        }));
        self.add_action(&action_hide_left_side_bar);
    }

    pub fn resotre_data(&self) {
        if let Ok(file) = File::open(data_path(".credential")) {
            let backup_data: Vec<SessionCredential> = serde_json::from_reader(file)
                .expect("Read backup data from json file `.credential` error.");
            self.imp()
                .session_credential_management
                .restore_session_credentials(backup_data);
        }
    }

    pub fn with_new_session_dialog<T>(&self, f: T)
    where
        T: FnOnce(&super::new_session_dialog::NewSessionDialog),
    {
        f(self.imp().new_session_dialog.get().expect(""))
    }

    pub fn with_session_credential_management<T>(&self, f: T)
    where
        T: FnOnce(&super::session_credential_tree::SessionCredentialManagementTree),
    {
        f(self.imp().session_credential_management.as_ref())
    }

    pub fn with_terminal_emulator<T>(&self, f: T)
    where
        T: FnOnce(&super::terminal::NativeTerminalEmulator),
    {
        f(self.imp().native_terminal_emulator.as_ref())
    }
}
