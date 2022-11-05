use crate::{CredentialType, SessionCredential, SessionCredentialGroup};

pub struct AuthApi;

impl AuthApi {
    pub fn create_credential_group(group_name: &'static str) {
        let _credential_group = SessionCredentialGroup::new(group_name);
    }

    pub fn delete_credential_group(_group_name: &'static str) {}

    pub fn add_credential(
        host: &'static str,
        user: &'static str,
        password: &'static str,
        group: &'static str,
        port: i32,
        credential_type: CredentialType,
    ) {
        let _credential =
            SessionCredential::new(host, user, password, group, port, credential_type);
    }

    pub fn delete_credential() {}

    pub fn save_credential() {}

    pub fn resotre_credential() {}
}
