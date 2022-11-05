
use gtk::glib::once_cell::sync::OnceCell;
use gtk::subclass::prelude::*;
use gtk::{glib, Dialog};

#[derive(Default)]
pub struct NewSessionDialog {
    pub dialog: OnceCell<Dialog>
}

#[glib::object_subclass]
impl ObjectSubclass for NewSessionDialog {
    const NAME: &'static str = "NewSessionDialog";

    type Type = super::NewSessionDialog;
}

impl ObjectImpl for NewSessionDialog {}