#![allow(dead_code)]
use super::TermioCommunityWindow;
use crate::ui::SessionCredentialManagementTree;
use gtk::glib::once_cell::sync::OnceCell;

pub const UI: UIHolderWrapper = UIHolderWrapper {
    _holder: OnceCell::new(),
};
pub const ERROR: &str = "`_holder` of UI should initialize first before use.";

#[derive(Debug)]
pub struct UIHolder {
    window: TermioCommunityWindow,
    session_credential_management: SessionCredentialManagementTree,
}
pub struct UIHolderWrapper {
    pub _holder: OnceCell<UIHolder>,
}

impl UIHolder {
    pub fn create(
        window: TermioCommunityWindow,
        session_credential_management: SessionCredentialManagementTree,
    ) -> Self {
        UIHolder {
            window,
            session_credential_management,
        }
    }
}

impl UIHolderWrapper {
    pub fn termio_community_window(&self) -> &TermioCommunityWindow {
        &self._holder.get().expect(ERROR).window
    }

    pub fn session_credential_management(&self) -> &SessionCredentialManagementTree {
        &self._holder.get().expect(ERROR).session_credential_management
    }
}
