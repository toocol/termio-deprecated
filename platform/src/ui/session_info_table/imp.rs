use gtk::{
    glib::{self, once_cell::sync::OnceCell},
    subclass::prelude::*, ListStore, traits::TreeViewExt,
};

#[derive(Default)]
pub struct SessionInfoTable {
    pub list_store: OnceCell<ListStore>,
}

#[glib::object_subclass]
impl ObjectSubclass for SessionInfoTable {
    const NAME: &'static str = "SessionInfoTable";

    type Type = super::SessionInfoTable;

    type ParentType = gtk::TreeView;
}

impl ObjectImpl for SessionInfoTable {
    fn constructed(&self) {
        self.parent_constructed();

        let obj = self.instance();
        obj.set_headers_visible(false);
        obj.setup_columns();
        obj.setup_model();
        obj.create_session_info_table();
    }
}

impl WidgetImpl for SessionInfoTable {}

impl TreeViewImpl for SessionInfoTable {}
