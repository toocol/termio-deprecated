use kernel::ProtocolType;
use std::cell::{Cell, RefCell};

use gtk::glib::once_cell::sync::{Lazy, OnceCell};
use gtk::glib::{self, ParamSpec, ParamSpecInt, ParamSpecString, ParamSpecUInt, Value};
use gtk::prelude::ToValue;
use gtk::subclass::prelude::*;

#[derive(Default)]
pub struct SessionCredentialObject {
    pub id: OnceCell<i32>,
    pub shown_name: RefCell<String>,
    pub host: RefCell<String>,
    pub user: RefCell<String>,
    pub password: RefCell<String>,
    pub group: RefCell<String>,
    pub port: Cell<u32>,
    pub protocol: OnceCell<ProtocolType>,
}

#[glib::object_subclass]
impl ObjectSubclass for SessionCredentialObject {
    const NAME: &'static str = "SessionCredentialObject";

    type Type = super::SessionCredentialObject;
}

impl ObjectImpl for SessionCredentialObject {
    fn properties() -> &'static [ParamSpec] {
        static PROPERTIES: Lazy<Vec<ParamSpec>> = Lazy::new(|| {
            vec![
                ParamSpecString::builder("shown-name").build(),
                ParamSpecString::builder("host").build(),
                ParamSpecString::builder("user").build(),
                ParamSpecString::builder("password").build(),
                ParamSpecString::builder("group").build(),
                ParamSpecUInt::builder("port").build(),
                ParamSpecInt::builder("protocol").build(),
            ]
        });
        PROPERTIES.as_ref()
    }

    fn set_property(&self, _id: usize, value: &Value, pspec: &ParamSpec) {
        match pspec.name() {
            "shown-name" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.shown_name.replace(input_value);
            }
            "host" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.host.replace(input_value);
            }
            "user" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.user.replace(input_value);
            }
            "password" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.password.replace(input_value);
            }
            "group" => {
                let input_value = value
                    .get()
                    .expect("The value needs to be of type `String`.");
                self.group.replace(input_value);
            }
            "port" => {
                let input_value = value.get().expect("The value needs to be of type `u32`.");
                self.port.set(input_value);
            }
            "protocol" => {
                let input_value: i32 = value.get().expect("The value needs to be of type `i8`.");
                let credential_type = ProtocolType::from_int(input_value);
                self.protocol
                    .set(credential_type)
                    .expect("`protocol` of `SessionCredentialObject` can only set once.");
            }
            _ => unimplemented!(),
        }
    }

    fn property(&self, _id: usize, pspec: &ParamSpec) -> Value {
        match pspec.name() {
            "shown-name" => self.shown_name.borrow().to_value(),
            "host" => self.host.borrow().to_value(),
            "user" => self.user.borrow().to_value(),
            "password" => self.password.borrow().to_value(),
            "group" => self.group.borrow().to_value(),
            "port" => self.port.get().to_value(),
            "protocol" => self
                .protocol
                .get()
                .expect("`protocol` should initialize first before use.")
                .to_int()
                .to_value(),
            _ => unimplemented!(),
        }
    }
}
