mod imp;

use core::{CredentialType, SessionCredential};

use glib::Object;
use gtk::glib;

glib::wrapper! {
    pub struct SessionCredentialObject(ObjectSubclass<imp::SessionCredentialObject>);
}

impl SessionCredentialObject {
    pub fn new(
        _host: &'static str,
        _user: &'static str,
        _password: &'static str,
        _group: &'static str,
        _port: i32,
        _credential_type: CredentialType,
    ) -> Self {
        Object::new(&[])
    }

    pub fn from_session_credential(session_credential: SessionCredential) -> Self {
        SessionCredentialObject::new(
            session_credential.host,
            session_credential.user,
            session_credential.password,
            session_credential.group,
            session_credential.port,
            session_credential.credential_type
        )
    }
}
