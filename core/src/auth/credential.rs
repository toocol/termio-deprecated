use serde::{Deserialize, Serialize};

pub const CREDENTIAL_TYPE_SSH: i8 = 1;
pub const CREDENTIAL_TYPE_MOSH: i8 = 2;

#[derive(Serialize, Deserialize, Debug)]
pub enum CredentialType {
    SSH,
    MOSH,
}

pub fn match_credential_type(ctype: i8) -> CredentialType {
    match ctype {
        CREDENTIAL_TYPE_SSH => CredentialType::SSH,
        CREDENTIAL_TYPE_MOSH => CredentialType::MOSH,
        _ => panic!(
            "Credential type only support 1=[SSH], 2=[MOSH]. Get {}",
            ctype
        ),
    }
}

pub fn to_credential_type_const(credential_type: &CredentialType) -> i8 {
    match credential_type {
        CredentialType::SSH => CREDENTIAL_TYPE_SSH,
        CredentialType::MOSH => CREDENTIAL_TYPE_MOSH,
    }
}

pub struct SessionCredentialGroup {
    pub name: &'static str,
    pub credentials: Vec<SessionCredential>,
}

#[derive(Serialize, Deserialize)]
pub struct SessionCredential {
    id: i32,

    pub host: &'static str,
    pub user: &'static str,
    pub password: &'static str,
    pub group: &'static str,
    pub port: i32,
    pub credential_type: CredentialType,
}

impl SessionCredentialGroup {
    pub fn new(name: &'static str) -> Self {
        SessionCredentialGroup {
            name,
            credentials: vec![],
        }
    }
}

impl SessionCredential {
    pub fn new(
        host: &'static str,
        user: &'static str,
        password: &'static str,
        group: &'static str,
        port: i32,
        credential_type: CredentialType,
    ) -> Self {
        SessionCredential {
            id: 0,
            host,
            user,
            password,
            group,
            port,
            credential_type,
        }
    }
}
