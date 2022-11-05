mod imp;

use core::{to_credential_type_const, CredentialType, SessionCredential};

use glib::Object;
use gtk::glib;
use gtk::subclass::prelude::*;

glib::wrapper! {
    pub struct SessionCredentialObject(ObjectSubclass<imp::SessionCredentialObject>);
}

impl SessionCredentialObject {
    pub fn new(
        host: &str,
        user: &str,
        password: &str,
        group: &str,
        port: i32,
        credential_type: CredentialType,
    ) -> Self {
        Object::builder()
            .property("host", host)
            .property("user", user)
            .property("password", password)
            .property("group", group)
            .property("port", port)
            .property(
                "credential-type",
                to_credential_type_const(&credential_type),
            )
            .build()
    }

    pub fn from_session_credential(session_credential: SessionCredential) -> Self {
        SessionCredentialObject::new(
            &session_credential.host,
            &session_credential.user,
            &session_credential.password,
            &session_credential.group,
            session_credential.port,
            session_credential.credential_type,
        )
    }

    pub fn to_session_credetial(&self) -> SessionCredential {
        let obj = self.imp();
        SessionCredential::new(
            obj.host.borrow().clone(),
            obj.user.borrow().clone(),
            obj.password.borrow().clone(),
            obj.group.borrow().clone(),
            obj.port.get(),
            obj.credential_type
                .get()
                .expect("`credential_type` should initialize first before use.")
                .clone(),
        )
    }
}
