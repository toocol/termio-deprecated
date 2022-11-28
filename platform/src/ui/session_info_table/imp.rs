use gtk::{
    glib::{self, once_cell::sync::OnceCell},
    subclass::prelude::*,
    TreeStore,
};

#[derive(Default)]
pub struct SessionInfoTable {
    pub tree_store: OnceCell<TreeStore>,
}

#[glib::object_subclass]
impl ObjectSubclass for SessionInfoTable {
    const NAME: &'static str = "SessionInfoTable";

    type Type = super::SessionInfoTable;

    type ParentType = gtk::TreeView;
}

impl ObjectImpl for SessionInfoTable {}

impl WidgetImpl for SessionInfoTable {}

impl TreeViewImpl for SessionInfoTable {}
