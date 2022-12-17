mod imp;

use kernel::{ProtocolType, SessionCredential, create_session};
use std::fs::File;

use gtk::{
    gdk::Rectangle,
    gio::{self, SimpleAction},
    glib::{self, clone, Object, VariantTy},
    prelude::*,
    subclass::prelude::*,
    Application, Widget,
};

use platform::{
    termio::data_path, GtkMouseButton, ItemStatus, QtMouseButton,
    ShellStartupMenu, ACTION_ADD_SESSION_CREDENTIAL, ACTION_COMMAND_ADD, ACTION_CREATE_SSH_SESSION,
    ACTION_HIDE_LEFT_SIDE_BAR, ACTION_LOCALE_CHANGED, ACTION_NEW_SESSION_CREDENTIAL_DIALOG,
    ACTION_RIGHT_CLICK_TERMINAL_TAB, ACTION_SESSION_CREDENTIAL_SELECTION_CHANGE,
    ACTION_SESSION_GROUP_SELECTION_CHANGE, ACTION_TAB_BUTTON_MOUSE_PRESS,
    ACTION_TAB_BUTTON_MOUSE_RELEASE, ACTION_TOGGLE_BOTTOM_AREA, ACTION_TOGGLE_COMMAND_PANEL,
    ACTION_TOGGLE_LEFT_AREA, ACTION_TOGGLE_PLUGIN_EXTENSION_PANEL,
    ACTION_TOGGLE_SESSION_MANAGEMENT_PANEL, ACTION_TOGGLE_SETTING_PANEL, ACTION_SHELL_STARTUP,
};

use platform::NewSessionDialog;

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

        let shell_startup_menu = ShellStartupMenu::builder()
            .position(gtk::PositionType::Bottom)
            .has_arrow(false)
            .build();
        self.imp().native_terminal_emulator.with_node(
            clone!(@weak shell_startup_menu => move |node| {
                shell_startup_menu.set_parent(node);
            }),
        );
        self.imp()
            .shell_startup_menu
            .set(shell_startup_menu)
            .expect("`shell_startup_menu` of `TermioCommunityWindow` can only set once.");
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
            if window.imp().workspace_activity_bar.get_visible() {
                window.imp()
                    .workspace_activity_bar
                    .hide_activity_bar(window.imp().workspace_left_side_bar.as_ref());
                window.imp().left_side_bar_seperator.hide();
            } else {
                window.imp()
                    .workspace_activity_bar
                    .show_activity_bar(window.imp().workspace_left_side_bar.as_ref());
                window.imp().left_side_bar_seperator.show();
            }
        }));
        self.add_action(&action_toggle_bottom_area);

        // Create `hide-left-side-bar` action.
        let action_hide_left_side_bar = SimpleAction::new(
            ACTION_HIDE_LEFT_SIDE_BAR.create(),
            Some(&String::static_variant_type()),
        );
        action_hide_left_side_bar.connect_activate(
            clone!(@weak self as window => move |_, parameter| {
                let param = parameter
                        .expect("Could not get parameter.")
                        .get::<String>()
                        .expect("The variant needs to be of type `String`.");
                window.imp().workspace_activity_bar.set_current_activate_widget(None);
                window.imp().workspace_activity_bar.toggle_item(param.as_str());
                window.imp().workspace_left_side_bar.hide();
                let allocation = window.imp()
                    .workspace_terminal_scrolled_window
                    .allocation();
                window.imp()
                    .native_terminal_emulator
                    .resize(allocation.width(), allocation.height());
            }),
        );
        self.add_action(&action_hide_left_side_bar);

        // Create `toggle-session-management-panel` action.
        let action_toggle_session_management_panel = SimpleAction::new(
            ACTION_TOGGLE_SESSION_MANAGEMENT_PANEL.create(),
            Some(&VariantTy::TUPLE),
        );
        action_toggle_session_management_panel.connect_activate(
            clone!(@weak self as window => move |_, parameter| {
                let param = parameter
                    .expect("Could not get parameter.")
                    .get::<(String, u8)>()
                    .expect("The variant needs to be of type `tuple`.");
                window.imp().workspace_activity_bar.set_item_status_off_except(param.0.as_str());
                let status = ItemStatus::from_u8(param.1);
                match status {
                    ItemStatus::On => {
                        window.imp().workspace_activity_bar.set_current_activate_widget(Some(param.0));
                        window.imp().workspace_left_side_bar.show();
                    },
                    ItemStatus::Off => {
                        window.imp().workspace_activity_bar.set_current_activate_widget(None);
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
        let action_toggle_plugin_extension_panel = SimpleAction::new(
            ACTION_TOGGLE_PLUGIN_EXTENSION_PANEL.create(),
            Some(&VariantTy::TUPLE),
        );
        action_toggle_plugin_extension_panel.connect_activate(
            clone!(@weak self as window => move |_, parameter| {
                let param = parameter
                    .expect("Could not get parameter.")
                    .get::<(String, u8)>()
                    .expect("The variant needs to be of type `tuple`.");
                window.imp().workspace_activity_bar.set_item_status_off_except(param.0.as_str());
            }),
        );
        self.add_action(&action_toggle_plugin_extension_panel);

        // Create `toggle-setting-panel` action.
        let action_toggle_setting_panel =
            SimpleAction::new(ACTION_TOGGLE_SETTING_PANEL.create(), None);
        action_toggle_setting_panel.connect_activate(clone!(@weak self as window => move |_, _| {

        }));
        self.add_action(&action_toggle_setting_panel);

        // Create `toggle-command-panel` action.
        let action_toggle_command_panel =
            SimpleAction::new(ACTION_TOGGLE_COMMAND_PANEL.create(), None);
        action_toggle_command_panel.connect_activate(clone!(@weak self as window => move |_, _| {
            if window.imp().command_panel_revealer.is_visible() {
                window.imp().command_panel_revealer.set_reveal_child(false);
                window.imp().command_panel_revealer.set_visible(false);
                window.imp().native_terminal_emulator.grab_focus();
                window.imp().command_panel.clear_entry();
            } else {
                window.imp().command_panel_revealer.set_visible(true);
                window.imp().command_panel_revealer.set_reveal_child(true);
                window.imp().command_panel.entry_grab_focus();
            }
        }));
        self.add_action(&action_toggle_command_panel);

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
                        .expect("The variant needs to be of type `tuple`.");
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
                        .expect("The variant needs to be of type `tuple`.");
                window.with_terminal_emulator(move |emulator| {
                    emulator.create_ssh_session(
                        param.0,            // session id
                        param.1.as_str(),   // host
                        param.2.as_str(),   // username
                        param.3.as_str(),   // password
                    );
                    emulator.grab_focus();
                })
            }),
        );
        self.add_action(&action_create_ssh_session);

        // Create `locale-changed` action.
        let action_locale_changed = SimpleAction::new(ACTION_LOCALE_CHANGED.create(), None);
        action_locale_changed.connect_activate(clone!(@weak self as window => move |_, _| {

        }));
        self.add_action(&action_locale_changed);

        // Create `session-credential-selection-change` action.
        let action_session_credential_selection_change = SimpleAction::new(
            ACTION_SESSION_CREDENTIAL_SELECTION_CHANGE.create(),
            Some(VariantTy::TUPLE),
        );
        action_session_credential_selection_change.connect_activate(
            clone!(@weak self as window => move |_, parameter| {
                let param = parameter
                    .expect("Could not get parameter.")
                    .get::<(String, String, String, i32, u32)>()
                    .expect("The variant needs to be of type `tuple`.");
                window.imp()
                    .session_info_table
                    .update_session_credential_info_table(
                        param.0.as_str(),
                        param.1.as_str(),
                        param.2.as_str(),
                        ProtocolType::from_int(param.3),
                        param.4
                );
            }),
        );
        self.add_action(&action_session_credential_selection_change);

        // Create `session-group-selection-change` action.
        let action_session_group_selection_change = SimpleAction::new(
            ACTION_SESSION_GROUP_SELECTION_CHANGE.create(),
            Some(VariantTy::TUPLE),
        );
        action_session_group_selection_change.connect_activate(
            clone!(@weak self as window => move |_, parameter| {
                let param = parameter
                    .expect("Could not get parameter.")
                    .get::<(String, i32)>()
                    .expect("The variant needs to be of type `tuple`.");
                window.imp().session_info_table
                    .update_session_group_info_table(param.0.as_str(), param.1);
            }),
        );
        self.add_action(&action_session_group_selection_change);

        // Create `action-command-add` action.
        let action_command_add = SimpleAction::new(ACTION_COMMAND_ADD.create(), None);
        action_command_add.connect_activate(clone!(@weak self as window => move |_, _| {
            window.imp()
                .new_session_dialog
                .get()
                .expect("`new_session_dialog` is None.")
                .show_dialog();
        }));
        self.add_action(&action_command_add);

        // Create `right-click-terminal-tab` action.
        let action_right_click_terminal_tab =
            SimpleAction::new(ACTION_RIGHT_CLICK_TERMINAL_TAB.create(), None);
        action_right_click_terminal_tab
            .connect_activate(clone!(@weak self as window => move |_, _| {
            }));
        self.add_action(&action_right_click_terminal_tab);

        // Create `tab-button-mouse-press` action.
        let action_tab_button_mouse_press = SimpleAction::new(
            ACTION_TAB_BUTTON_MOUSE_PRESS.create(),
            Some(VariantTy::ARRAY),
        );
        action_tab_button_mouse_press.connect_activate(
            clone!(@weak self as window => move |_, parameter| {
                let param = parameter
                    .expect("Could not get parameter.")
                    .get::<Vec<String>>()
                    .expect("The variant needs to be of type `vec`.");
                println!("Tab button mouse press. param = {:?}", param);
            }),
        );
        self.add_action(&action_tab_button_mouse_press);

        // Create `tab-button-mouse-release` action.
        let action_tab_button_mouse_release = SimpleAction::new(
            ACTION_TAB_BUTTON_MOUSE_RELEASE.create(),
            Some(VariantTy::ARRAY),
        );
        action_tab_button_mouse_release.connect_activate(
            clone!(@weak self as window => move |_, parameter| {
                let param = parameter
                    .expect("Could not get parameter.")
                    .get::<Vec<String>>()
                    .expect("The variant needs to be of type `vec`.");
                println!("Tab button mouse release. param = {:?}", param);
                let tab_button_name = param[0].as_str();
                let mouse_button = QtMouseButton::from_i32(param[1].parse().unwrap()).to_gtk_button();

                match mouse_button {
                    GtkMouseButton::Left => {
                        match tab_button_name {
                            "tab-button-new" => {
                                let (x, _) = window.imp()
                                    .native_terminal_emulator
                                    .last_left_mouse_release_position();
                                    
                                let shell_startup_window = window.imp()
                                    .shell_startup_menu
                                    .get()
                                    .expect("`shell_startup_menu` of `TermioCommunityWindow` is None.");
                                shell_startup_window
                                    .set_pointing_to(Some(&Rectangle::new(x, 22, 1, 1)));
                                shell_startup_window.show();
                            },
                            _ => {},
                        }
                    },
                    GtkMouseButton::Right => {},
                    GtkMouseButton::Middle => {},
                    GtkMouseButton::All => {},
                    GtkMouseButton::NoButton => {},
                }
            }),
        );
        self.add_action(&action_tab_button_mouse_release);

        // Create `shell-startup` action.
        let action_shell_startup = SimpleAction::new(ACTION_SHELL_STARTUP.create(), Some(&VariantTy::STRING));
        action_shell_startup.connect_activate(clone!(@weak self as window => move |_, parameter| {
            let param = parameter
                .expect("Could not get parameter.")
                .get::<String>()
                .expect("The variant needs to be of type `String`.");
            let session_id = create_session(ProtocolType::LocalShell);
            window.imp().shell_startup_menu.get().expect("`shell_startup_menu` is None.").hide();
            window.imp().native_terminal_emulator.shell_startup(session_id, param.as_str());
        }));
        self.add_action(&action_shell_startup);
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

    pub fn chlildren<T: IsA<Widget>>(&self, widget: &T) -> Vec<Widget> {
        let mut children = vec![];
        if let Some(first_child) = widget.first_child() {
            children.push(first_child);
            let mut first_child = &children[children.len() - 1];
            while let Some(sibling) = first_child.next_sibling() {
                children.push(sibling);
                first_child = &children[children.len() - 1];
            }
        }
        children
    }

    pub fn generic_chlildren<T: IsA<Widget>, R: IsA<Widget>>(&self, widget: &T) -> Vec<R> {
        let mut children = vec![];
        if let Some(first_child) = widget.first_child() {
            let item = first_child
                .downcast::<R>()
                .expect("Downcast failed type mismatch");
            children.push(item);
            let mut first_child = &children[children.len() - 1];
            while let Some(sibling) = first_child.next_sibling() {
                let item = sibling
                    .downcast::<R>()
                    .expect("Downcast failed type mismatch");
                children.push(item);
                first_child = &children[children.len() - 1];
            }
        }
        children
    }
}
