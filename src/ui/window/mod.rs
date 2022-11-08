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
use platform::SessionCredentialObject;

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
        action_new_session_credential.connect_activate(clone!(@weak self as scmt => move |_, _| {
            scmt
                .imp()
                .new_session_dialog
                .get()
                .expect("`new_session_dialog` of `TermioCommunityWindow` must initialized first before use.")
                .show_dialog();
        }));
        self.add_action(&action_new_session_credential);
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

    pub fn new_session_credential(
        &self,
        shown_name: &str,
        host: &str,
        username: &str,
        password: &str,
        group: &str,
        port: u32,
    ) {
        let session_credential = SessionCredentialObject::new(
            shown_name,
            host,
            username,
            password,
            group,
            port,
            core::ProtocolType::Ssh,
        );
        self.imp()
            .session_credential_management
            .add_session_credential(session_credential);
    }
}
