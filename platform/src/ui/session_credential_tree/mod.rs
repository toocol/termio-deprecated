mod imp;

use core::{create_session, SessionCredential};

use crate::{SessionCredentialObject, ACTION_CREATE_SSH_SESSION, LanguageBundle, ACTION_SESSION_GROUP_SELECTION_CHANGE, ACTION_SESSION_CREDENTIAL_SELECTION_CHANGE};
use gtk::{
    glib::{self, Type},
    prelude::*,
    subclass::prelude::*,
    traits::TreeViewExt,
    CellAreaBox, CellRendererText, TreeStore, TreeViewColumn,
};
use log::debug;
use utilities::DynamicBundle;

glib::wrapper! {
    pub struct SessionCredentialManagementTree(ObjectSubclass<imp::SessionCredentialManagementTree>)
        @extends gtk::TreeView, gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget, gtk::Scrollable;
}

#[repr(u32)]
enum Columns {
    ShownName = 0,
    Host,
    Username,
    Password,
    Port,
    Type,
    Protocol,
}

#[repr(u8)]
enum TreeNodeType {
    Group = 1,
    SessionCredential = 2,
}
impl TreeNodeType {
    pub fn from_u8(node_type: u8) -> Self {
        match node_type {
            1 => TreeNodeType::Group,
            2 => TreeNodeType::SessionCredential,
            _ => unimplemented!(),
        }
    }
}

impl SessionCredentialManagementTree {
    pub fn setup_columns(&self) {
        // Column 0: shown_name
        let cell_renderer = CellRendererText::new();
        let cell_area = CellAreaBox::builder()
            .orientation(gtk::Orientation::Vertical)
            .build();
        cell_area.pack_start(&cell_renderer, false, true, true);
        let column = TreeViewColumn::builder()
            .cell_area(&cell_area)
            .expand(false)
            .build();
        column.add_attribute(&cell_renderer, "text", Columns::ShownName as i32);
        self.append_column(&column);
    }

    pub fn setup_model(&self) {
        let tree_store = TreeStore::new(&[
            Type::STRING,
            Type::STRING,
            Type::STRING,
            Type::STRING,
            Type::U32,
            Type::U8,
            Type::I32,
        ]);
        self.imp()
            .tree_store
            .set(tree_store)
            .expect("`tree_store` of `SessionCredentialManagementTree` can only set once.");
        self.set_model(Some(
            self.imp()
                .tree_store
                .get()
                .expect("`tree_store` should initialize first before use."),
        ));
    }

    pub fn setup_default_group(&self) {
        let tree_store = self.imp().tree_store.get().expect(
            "`tree_store` of `SessionCredentialManagementTree` must initialize before use.",
        );
        let default_group = tree_store.append(None);

        tree_store.set_value(
            &default_group,
            Columns::ShownName as u32,
            &LanguageBundle::message(LanguageBundle::KEY_TEXT_SESSION_DEFAULT_GROUP, None).to_value(),
        );
        tree_store.set_value(
            &default_group,
            Columns::Type as u32,
            &(TreeNodeType::Group as u8).to_value(),
        );

        self.imp()
            .group_map
            .borrow_mut()
            .insert("default".to_string(), default_group);
    }

    pub fn setup_callbacks(&self) {
        self.connect_row_activated(|tree_view, _, _| {
            let selection = tree_view.selection();
            if let Some((model, iter)) = selection.selected() {
                let node_type = model
                    .get_value(&iter, Columns::Type as i32)
                    .get::<u8>()
                    .expect("`SessionCredentialManagementTree` get `password` value error.");
                if node_type == TreeNodeType::SessionCredential as u8 {
                    let host = model
                        .get_value(&iter, Columns::Host as i32)
                        .get::<String>()
                        .expect("`SessionCredentialManagementTree` get `host` value error.");
                    let username = model
                        .get_value(&iter, Columns::Username as i32)
                        .get::<String>()
                        .expect("`SessionCredentialManagementTree` get `username` value error.");
                    let password = model
                        .get_value(&iter, Columns::Password as i32)
                        .get::<String>()
                        .expect("`SessionCredentialManagementTree` get `password` value error.");
                    let port = model
                        .get_value(&iter, Columns::Port as i32)
                        .get::<u32>()
                        .expect("`SessionCredentialManagementTree` get `password` value error.");
                    let session_id = create_session(core::ProtocolType::Ssh);
                    tree_view.activate_action(
                        &ACTION_CREATE_SSH_SESSION.activate(), 
                        Some(
                            &(
                                session_id, 
                                host.as_str(), 
                                username.as_str(), 
                                password.as_str()
                            ).to_variant()
                        )
                    ).expect(format!("Action `{}` activate failed.", ACTION_CREATE_SSH_SESSION.activate()).as_str());
                    debug!(
                        "Try to connect ssh session, session_id={}, host={}, username={}, password={}, port={}",
                        session_id, host, username, password, port
                    );
                }
            }
        });

        self.connect_cursor_changed(|tree_view| {
            let selection = tree_view.selection();
            if let Some((tree_model, iter)) = selection.selected() {
                let node_type = tree_model
                    .get_value(&iter, Columns::Type as i32)
                    .get::<u8>()
                    .expect("Columns `Type` value type mismatch.");
                let node_type = TreeNodeType::from_u8(node_type);

                let shown_name = tree_model
                    .get_value(&iter, Columns::ShownName as i32)
                    .get::<String>()
                    .expect("Columns `ShownName` value type mismatch.");

                match node_type {
                    TreeNodeType::Group => {
                        let child_num = tree_model.iter_n_children(Some(&iter));
                        let param = (shown_name, child_num);
                        tree_view.activate_action(&ACTION_SESSION_GROUP_SELECTION_CHANGE.activate(), Some(&param.to_variant()))
                            .expect(format!("Activate action `{}` failed.", ACTION_SESSION_GROUP_SELECTION_CHANGE.activate()).as_str());
                    },
                    TreeNodeType::SessionCredential => {
                        let host = tree_model
                            .get_value(&iter, Columns::Host as i32)
                            .get::<String>()
                            .expect("Columns `Host` value type mismatch.");
                        let username = tree_model
                            .get_value(&iter, Columns::Username as i32)
                            .get::<String>()
                            .expect("Columns `Username` value type mismatch.");
                        let protocol = tree_model
                            .get_value(&iter, Columns::Protocol as i32)
                            .get::<i32>()
                            .expect("Columns `Protocol` value type mismatch.");
                        let port = tree_model
                            .get_value(&iter, Columns::Port as i32)
                            .get::<u32>()
                            .expect("Columns `Port` value type mismatch.");
                        let param = (shown_name, host, username, protocol, port);

                        tree_view.activate_action(&ACTION_SESSION_CREDENTIAL_SELECTION_CHANGE.activate(), Some(&param.to_variant()))
                            .expect(format!("Activate action {} failed.", ACTION_SESSION_CREDENTIAL_SELECTION_CHANGE.activate()).as_str());
                    },
                }
            }
        });
    }

    pub fn add_session_credential(
        &self,
        shown_name: &str,
        host: &str,
        username: &str,
        password: &str,
        group: &str,
        port: u32,
    ) {
        let session_credential = SessionCredentialObject::new(
            shown_name,
            host,
            username,
            password,
            group,
            port,
            core::ProtocolType::Ssh,
        );
        let tree_store = self.imp().tree_store.get().expect(
            "`tree_store` of `SessionCredentialManagementTree` must initialize before use.",
        );
        if let Some(_) = self.add_session_credentials_to_model(tree_store, &session_credential) {
            self.imp()
                .session_credentials
                .borrow_mut()
                .push(session_credential);
        }
    }

    pub fn session_credentials(&self) -> Vec<SessionCredentialObject> {
        self.imp().session_credentials.borrow().clone()
    }

    pub fn restore_session_credentials(&self, backup_data: Vec<SessionCredential>) {
        let session_credentials: Vec<SessionCredentialObject> = backup_data
            .into_iter()
            .map(SessionCredentialObject::from_session_credential)
            .collect();

        let tree_store = self.imp().tree_store.get().expect(
            "`tree_store` of `SessionCredentialManagementTree` must initialize before use.",
        );

        for session_credential in session_credentials {
            if let Some(_) = self.add_session_credentials_to_model(tree_store, &session_credential)
            {
                self.imp()
                    .session_credentials
                    .borrow_mut()
                    .push(session_credential);
            }
        }
    }

    fn add_session_credentials_to_model(
        &self,
        tree_store: &TreeStore,
        session_credential: &SessionCredentialObject,
    ) -> Option<()> {
        if let Some(parent_iter) = self
            .imp()
            .group_map
            .borrow()
            .get(&session_credential.group())
        {
            let child_iter = tree_store.append(Some(parent_iter));
            tree_store.set_value(
                &child_iter,
                Columns::ShownName as u32,
                &session_credential.to_shown_string().to_value(),
            );
            tree_store.set_value(
                &child_iter,
                Columns::Host as u32,
                &session_credential.host().to_value(),
            );
            tree_store.set_value(
                &child_iter,
                Columns::Username as u32,
                &session_credential.username().to_value(),
            );
            tree_store.set_value(
                &child_iter,
                Columns::Password as u32,
                &session_credential.password().to_value(),
            );
            tree_store.set_value(
                &child_iter,
                Columns::Port as u32,
                &(session_credential.port() as i32).to_value(),
            );
            tree_store.set_value(
                &child_iter,
                Columns::Type as u32,
                &(TreeNodeType::SessionCredential as u8).to_value(),
            );
            tree_store.set_value(
                &child_iter,
                Columns::Protocol as u32,
                &(session_credential.protocol().to_int()).to_value(),
            );
            debug!(
                "Insert tree_iter success, shown_string: {}",
                &session_credential.to_shown_string()
            );
            return Some(());
        }
        None
    }
}
