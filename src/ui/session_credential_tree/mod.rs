mod imp;

use gtk::{
    glib::{self, Type},
    prelude::*,
    subclass::prelude::*,
    traits::TreeViewExt,
    CellAreaBox, CellRendererText, TreeStore, TreeViewColumn,
};
use log::debug;
use platform::SessionCredentialObject;

glib::wrapper! {
    pub struct SessionCredentialManagementTree(ObjectSubclass<imp::SessionCredentialManagementTree>)
        @extends gtk::TreeView, gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget, gtk::Scrollable;
}

impl SessionCredentialManagementTree {
    pub fn session_credentials(&self) -> TreeStore {
        self.imp()
            .session_credentials
            .get()
            .expect("`session_credentials` should initialize first before get.")
            .clone()
    }

    pub fn setup_columns(&self) {
        let cell_renderer = CellRendererText::new();
        let cell_area = CellAreaBox::builder()
            .orientation(gtk::Orientation::Vertical)
            .build();
        cell_area.pack_start(&cell_renderer, false, true, true);

        let column = TreeViewColumn::builder()
            .cell_area(&cell_area)
            .expand(false)
            .build();
        column.add_attribute(&cell_renderer, "text", 0);

        self.append_column(&column);
    }

    pub fn setup_model(&self) {
        let tree_store = TreeStore::new(&[Type::STRING]);
        self.imp().session_credentials.set(tree_store).expect(
            "`session_credentials` of `SessionCredentialManagementTree` can only set once.",
        );
        self.set_model(Some(
            self.imp()
                .session_credentials
                .get()
                .expect("`session_credentials` should initialize first before use."),
        ));
    }

    pub fn setup_default_group(&self) {
        let tree_store = self.imp().session_credentials
            .get()
            .expect("`session_credentials` of `SessionCredentialManagementTree` must initialize before use.");
        let default_group = tree_store.append(None);

        tree_store.set_value(&default_group, 0, &"Default".to_value());

        self.imp()
            .group_map
            .borrow_mut()
            .insert("default".to_string(), default_group);
    }

    pub fn add_session_credential(&self, session_credential: SessionCredentialObject) {
        let tree_store = self.imp().session_credentials
            .get()
            .expect("`session_credentials` of `SessionCredentialManagementTree` must initialize before use.");
        if let Some(parent_iter) = self
            .imp()
            .group_map
            .borrow()
            .get(&session_credential.group())
        {
            let child_iter = tree_store.append(Some(parent_iter));
            tree_store.set_value(
                &child_iter,
                0,
                &session_credential.to_shown_string().to_value(),
            );
            debug!(
                "Insert tree_iter success, shown_string: {}",
                &session_credential.to_shown_string()
            );
        }
    }

    pub fn restore_session_credentials(&self) {}
}
