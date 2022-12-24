use cli::Command;

use gtk::{
    glib::{self, once_cell::sync::OnceCell},
    prelude::*,
    subclass::prelude::*,
    Dialog, Frame, Label,
};
use libs::DynamicBundle;

use crate::{LanguageBundle, ACTION_COMMAND_ADD};

#[derive(Default)]
pub struct NewSessionDialog {
    pub dialog: OnceCell<Dialog>,

    pub host_label: OnceCell<Label>,
    pub username_label: OnceCell<Label>,
    pub password_label: OnceCell<Label>,
    pub port_label: OnceCell<Label>,

    pub basic_ssh_frame: OnceCell<Frame>,
}

impl NewSessionDialog {
    pub fn bind_multilingual_widget(
        &self,
        host_label: Label,
        username_label: Label,
        password_label: Label,
        port_label: Label,
        basic_ssh_frame: Frame,
    ) {
        self.host_label
            .set(host_label)
            .expect("`host_label` of NewSessionDialog can only set once.");
        self.username_label
            .set(username_label)
            .expect("`username_label` of NewSessionDialog can only set once.");
        self.password_label
            .set(password_label)
            .expect("`password_label` of NewSessionDialog can only set once.");
        self.port_label
            .set(port_label)
            .expect("`port_label` of NewSessionDialog can only set once.");
        self.basic_ssh_frame
            .set(basic_ssh_frame)
            .expect("`basic_ssh_frame` of NewSessionDialog can only set once.")
    }

    pub fn change_multilingual_text(&self) {
        self.host_label
            .get()
            .expect("`host_label` is None")
            .set_label(
                LanguageBundle::message(LanguageBundle::KEY_TEXT_REMOTE_HOST, None).as_str(),
            );

        self.username_label
            .get()
            .expect("`username_label` is None")
            .set_label(LanguageBundle::message(LanguageBundle::KEY_TEXT_USERNAME, None).as_str());

        self.password_label
            .get()
            .expect("`password_label` is None")
            .set_label(LanguageBundle::message(LanguageBundle::KEY_TEXT_PASSWORD, None).as_str());

        self.port_label
            .get()
            .expect("`port_label` is None")
            .set_label(LanguageBundle::message(LanguageBundle::KEY_TEXT_PORT, None).as_str());

        self.basic_ssh_frame
            .get()
            .expect("`port_label` is None")
            .set_label(Some(
                LanguageBundle::message(LanguageBundle::KEY_TEXT_PORT, None).as_str(),
            ));
    }
}

#[glib::object_subclass]
impl ObjectSubclass for NewSessionDialog {
    const NAME: &'static str = "NewSessionDialog";

    type Type = super::NewSessionDialog;
}

impl ObjectImpl for NewSessionDialog {
    fn constructed(&self) {
        self.parent_constructed();

        Command::new(
            "add",
            LanguageBundle::KEY_COMMAND_COMMENT_ADD.to_string(),
            ACTION_COMMAND_ADD.activate(),
            None,
            Some(vec!["Ctrl", "A"]),
        )
        .register();
    }
}
