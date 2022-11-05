mod imp;

use core::SessionCredential;
use gtk::glib;

glib::wrapper! {
    pub struct SessionCredentialManagementTree(ObjectSubclass<imp::SessionCredentialManagementTree>)
        @extends gtk::TreeView, gtk::Widget, 
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget, gtk::Scrollable;
}

impl SessionCredentialManagementTree {
    pub fn add_session_credential(&self, _session_credential: SessionCredential) {

    }
}