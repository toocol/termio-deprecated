use serde::{Deserialize, Serialize};

use crate::ProtocolType;

#[derive(Serialize, Deserialize)]
pub struct SessionCredential {
    id: i32,

    pub shown_name: String,
    pub host: String,
    pub user: String,
    pub password: String,
    pub group: String,
    pub port: u32,
    pub credential_type: ProtocolType,
}

impl SessionCredential {
    pub fn new(
        shown_name: String,
        host: String,
        user: String,
        password: String,
        group: String,
        port: u32,
        credential_type: ProtocolType,
    ) -> Self {
        SessionCredential {
            id: 0,
            shown_name,
            host,
            user,
            password,
            group,
            port,
            credential_type,
        }
    }
}
