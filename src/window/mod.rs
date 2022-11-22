mod imp;

use core::SessionCredential;
use std::fs::File;

use gtk::{
    gio::{self, SimpleAction},
    glib::{self, clone, Object, VariantTy},
    prelude::*,
    subclass::prelude::*,
    Application,
};

use platform::{
    termio::data_path, ItemStatus, ACTION_ADD_SESSION_CREDENTIAL, ACTION_CREATE_SSH_SESSION,
    ACTION_HIDE_LEFT_SIDE_BAR, ACTION_NEW_SESSION_CREDENTIAL_DIALOG, ACTION_TOGGLE_BOTTOM_AREA,
    ACTION_TOGGLE_LEFT_AREA, ACTION_TOGGLE_PLUGIN_EXTENSION_PANEL,
    ACTION_TOGGLE_SESSION_MANAGEMENT_PANEL, ACTION_TOGGLE_SETTING_PANEL,
};

use platform::NewSessionDialog;
use utilities::TimeStamp;

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
        // Create `toggle-bottom-area` action.
        let action_toggle_bottom_area = SimpleAction::new(ACTION_TOGGLE_BOTTOM_AREA.create(), None);
        action_toggle_bottom_area.connect_activate(clone!(@weak self as window => move |_, _| {

        }));
        self.add_action(&action_toggle_bottom_area);

        // Create `toggle-left-area` action.
        let action_toggle_bottom_area = SimpleAction::new(ACTION_TOGGLE_LEFT_AREA.create(), None);
        action_toggle_bottom_area.connect_activate(clone!(@weak self as window => move |_, _| {
            if window.imp().workspace_left_side_bar.get_visible() {
                window.imp().workspace_left_side_bar.hide();
            } else {
                window.imp().workspace_left_side_bar.show();
            }
            if window.imp().workspace_activity_bar.get_visible() {
                window.imp().workspace_activity_bar.hide();
            } else {
                window.imp().workspace_activity_bar.show();
            }
        }));
        self.add_action(&action_toggle_bottom_area);

        // Create `hide-left-side-bar` action.
        let action_hide_left_side_bar = SimpleAction::new(ACTION_HIDE_LEFT_SIDE_BAR.create(), None);
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

        // Create `toggle-session-management-panel` action.
        let action_toggle_session_management_panel = SimpleAction::new(
            ACTION_TOGGLE_SESSION_MANAGEMENT_PANEL.create(),
            Some(&u8::static_variant_type()),
        );
        action_toggle_session_management_panel.connect_activate(
            clone!(@weak self as window => move |_, parameter| {
                let status = parameter
                    .expect("Could not get parameter.")
                    .get::<u8>()
                    .expect("The variant needs to be of type `u8`.");
                let status = ItemStatus::from_u8(status);
                match status {
                    ItemStatus::On => {
                        window.imp().workspace_left_side_bar.show();
                    },
                    ItemStatus::Off => {
                        window.imp().workspace_left_side_bar.hide();
                    },
                }
                let allocation = window.imp()
                    .workspace_terminal_scrolled_window
                    .allocation();
                window.imp()
                    .native_terminal_emulator
                    .resize(allocation.width(), allocation.height());
            }),
        );
        self.add_action(&action_toggle_session_management_panel);

        // Create `toggle-plugin-extensions-panel` action.
        let action_toggle_plugin_extension_panel =
            SimpleAction::new(ACTION_TOGGLE_PLUGIN_EXTENSION_PANEL.create(), None);
        action_toggle_plugin_extension_panel.connect_activate(
            clone!(@weak self as window => move |_, _| {

            }),
        );
        self.add_action(&action_toggle_plugin_extension_panel);

        // Create `toggle-setting-panel` action.
        let action_toggle_setting_panel =
            SimpleAction::new(ACTION_TOGGLE_SETTING_PANEL.create(), None);
        action_toggle_setting_panel.connect_activate(clone!(@weak self as window => move |_, _| {

        }));
        self.add_action(&action_toggle_setting_panel);

        // Create `new-session-credential` action.
        let action_new_session_credential =
            SimpleAction::new(ACTION_NEW_SESSION_CREDENTIAL_DIALOG.create(), None);
        action_new_session_credential.connect_activate(clone!(@weak self as window => move |_, _| {
            window.imp()
                .new_session_dialog
                .get()
                .expect("`new_session_dialog` of `TermioCommunityWindow` must initialized first before use.")
                .show_dialog();
        }));
        self.add_action(&action_new_session_credential);

        // Create `add-session-credential` action.
        let action_add_session_credential = SimpleAction::new(
            ACTION_ADD_SESSION_CREDENTIAL.create(),
            Some(VariantTy::TUPLE),
        );
        action_add_session_credential.connect_activate(
            clone!(@weak self as window => move |_, parameter| {
                let param = parameter
                        .expect("Could not get parameter.")
                        .get::<(String, String, String, String, String, u32)>()
                        .expect("The variant needs to be of type `u8`.");
                window.with_session_credential_management(move |managemnet| {
                    managemnet.add_session_credential(
                        param.0.as_str(), // shown name
                        param.1.as_str(), // host
                        param.2.as_str(), // username
                        param.3.as_str(), // password
                        param.4.as_str(), // group
                        param.5           // port
                    );
                });
            }),
        );
        self.add_action(&action_add_session_credential);

        // Create `create-ssh-session` action.
        let action_create_ssh_session =
            SimpleAction::new(ACTION_CREATE_SSH_SESSION.create(), Some(VariantTy::TUPLE));
        action_create_ssh_session.connect_activate(
            clone!(@weak self as window => move |_, parameter| {
                let param = parameter
                        .expect("Could not get parameter.")
                        .get::<(u64, String, String, String,)>()
                        .expect("The variant needs to be of type `u8`.");
                window.with_terminal_emulator(move |emulator| {
                    emulator.create_ssh_session(
                        param.0,            // session id
                        param.1.as_str(),   // host
                        param.2.as_str(),   // username
                        param.3.as_str(),   // password
                        TimeStamp::timestamp()
                    );
                })
            }),
        );
        self.add_action(&action_create_ssh_session);
    }

    pub fn resotre_data(&self) {
        if let Ok(file) = File::open(data_path(
            ".credential",
            self.imp()
                .termio
                .get()
                .expect("`termio` of TermioCommunityWindow should set before use."),
        )) {
            let backup_data: Vec<SessionCredential> = serde_json::from_reader(file)
                .expect("Read backup data from json file `.credential` error.");
            self.imp()
                .session_credential_management
                .restore_session_credentials(backup_data);
        }
    }

    pub fn with_new_session_dialog<T>(&self, f: T)
    where
        T: FnOnce(&platform::NewSessionDialog),
    {
        f(self.imp().new_session_dialog.get().expect(""))
    }

    pub fn with_session_credential_management<T>(&self, f: T)
    where
        T: FnOnce(&platform::SessionCredentialManagementTree),
    {
        f(self.imp().session_credential_management.as_ref())
    }

    pub fn with_terminal_emulator<T>(&self, f: T)
    where
        T: FnOnce(&platform::NativeTerminalEmulator),
    {
        f(self.imp().native_terminal_emulator.as_ref())
    }
}
