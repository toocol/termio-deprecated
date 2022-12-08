mod imp;

use gtk::subclass::prelude::*;
use gtk::glib;
use glib::Object;

glib::wrapper! {
    pub struct MatchingLabel(ObjectSubclass<imp::CommandLabel>)
        @extends gtk::Widget,
        @implements gtk::Accessible, gtk::Buildable, gtk::ConstraintTarget;
}

impl MatchingLabel {
    pub fn new(command: String, matching: String) -> Self {
        let label: MatchingLabel = Object::new(&[]);
        label.imp().set_command_matching(command, matching);
        label
    }
}
