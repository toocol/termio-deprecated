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

pub const ACTION_HIDE_LEFT_SIDE_BAR: ActionName = ActionName {
    group: "win",
    name: "hide-left-side-bar",
};
pub const ACTION_TOGGLE_SESSION_MANAGEMENT_PANEL: ActionName = ActionName {
    group: "win",
    name: "toggle-session-management-panel",
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
