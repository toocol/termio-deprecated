mod imp;

use gtk::{
    glib::{self, Type},
    subclass::prelude::*,
    traits::TreeViewExt,
    TreeStore,
};

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

    pub fn setup_model(&self) {
        self.imp()
            .session_credentials
            .set(TreeStore::new(&[Type::STRING]))
            .expect(
                "`session_credentials` of `SessionCredentialManagementTree` can only set once.",
            );
        self.set_model(Some(
            self.imp()
                .session_credentials
                .get()
                .expect("`session_credentials` should initialize first before use."),
        ));
    }
}
