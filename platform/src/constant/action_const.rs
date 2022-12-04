pub const ACTION_TOGGLE_BOTTOM_AREA: ActionName = ActionName {
    group: "win",
    name: "toggle-bottom-area",
};
pub const ACTION_TOGGLE_LEFT_AREA: ActionName = ActionName {
    group: "win",
    name: "toggle-left-area",
};

pub const ACTION_HIDE_LEFT_SIDE_BAR: ActionName = ActionName {
    group: "win",
    name: "hide-left-side-bar",
};
pub const ACTION_TOGGLE_SESSION_MANAGEMENT_PANEL: ActionName = ActionName {
    group: "win",
    name: "toggle-session-management-panel",
};
pub const ACTION_TOGGLE_PLUGIN_EXTENSION_PANEL: ActionName = ActionName {
    group: "win",
    name: "toggle-plugin-extensions-panel",
};
pub const ACTION_TOGGLE_SETTING_PANEL: ActionName = ActionName {
    group: "win",
    name: "toggle-setting-panel",
};
pub const ACTION_TOGGLE_COMMAND_PANEL: ActionName = ActionName {
    group: "win",
    name: "toggle-command-panel",
};

pub const ACTION_NEW_SESSION_CREDENTIAL_DIALOG: ActionName = ActionName {
    group: "win",
    name: "new-session-credential-dialog",
};
pub const ACTION_ADD_SESSION_CREDENTIAL: ActionName = ActionName {
    group: "win",
    name: "add-session-credential",
};
pub const ACTION_CREATE_SSH_SESSION: ActionName = ActionName {
    group: "win",
    name: "create-ssh-session",
};

pub const ACTION_LOCALE_CHANGED: ActionName = ActionName {
    group: "win",
    name: "locale-changed",
};

pub const ACTION_SESSION_CREDENTIAL_SELECTION_CHANGE: ActionName = ActionName {
    group: "win",
    name: "session-credential-selection-change",
};
pub const ACTION_SESSION_GROUP_SELECTION_CHANGE: ActionName = ActionName {
    group: "win",
    name: "session-group-selection-change",
};

pub struct ActionName {
    group: &'static str,
    name: &'static str,
}

impl ActionName {
    pub fn create(&self) -> &str {
        self.name
    }

    pub fn activate(&self) -> String {
        let mut group = self.group.to_string();
        group.push('.');
        group.push_str(self.name);
        group
    }
}
