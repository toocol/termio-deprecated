use std::{cell::RefCell, collections::HashMap};

use gtk::{
    glib::{self, once_cell::sync::OnceCell},
    subclass::prelude::*,
    traits::TreeViewExt,
    TreeIter, TreeStore,
};
use crate::SessionCredentialObject;

#[derive(Default)]
pub struct SessionCredentialManagementTree {
    pub tree_store: OnceCell<TreeStore>,
    pub session_credentials: RefCell<Vec<SessionCredentialObject>>,
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
        instance.set_headers_visible(false);
        instance.setup_columns();
        instance.setup_model();
        instance.setup_default_group();
        instance.setup_callbacks();
    }
}

impl WidgetImpl for SessionCredentialManagementTree {}

impl TreeViewImpl for SessionCredentialManagementTree {}
