use std::{collections::HashMap, cell::RefCell};

use gtk::{
    glib::{self, once_cell::sync::OnceCell},
    subclass::prelude::*,
    TreeStore, TreeIter,
};

#[derive(Default)]
pub struct SessionCredentialManagementTree {
    pub session_credentials: OnceCell<TreeStore>,
    pub group_map: RefCell<HashMap<String, TreeIter>>,
}

#[glib::object_subclass]
impl ObjectSubclass for SessionCredentialManagementTree {
    const NAME: &'static str = "SessionCredentialManagementTree";

    type Type = super::SessionCredentialManagementTree;

    type ParentType = gtk::TreeView;
}

impl ObjectImpl for SessionCredentialManagementTree {
    fn constructed(&self) {
        self.parent_constructed();

        let instance = self.instance();
        instance.setup_columns();
        instance.setup_model();
        instance.setup_default_group();
    }
}

impl WidgetImpl for SessionCredentialManagementTree {}

impl TreeViewImpl for SessionCredentialManagementTree {}
