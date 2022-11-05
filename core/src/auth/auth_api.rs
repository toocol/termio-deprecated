use crate::{CredentialType, SessionCredential, SessionCredentialGroup};

pub struct AuthApi;

impl AuthApi {
    pub fn create_credential_group(group_name: &'static str) {
        let _credential_group = SessionCredentialGroup::new(group_name);
    }

    pub fn delete_credential_group(_group_name: &'static str) {}

    pub fn add_credential(
        host: &str,
        user: &str,
        password: &str,
        group: &str,
        port: i32,
        credential_type: CredentialType,
    ) {
        let _session_credential = SessionCredential::new(
            host.to_string(),
            user.to_string(),
            password.to_string(),
            group.to_string(),
            port,
            credential_type,
        );
    }

    pub fn delete_credential() {}

    pub fn save_credential() {}

    pub fn resotre_credential() {}
}
