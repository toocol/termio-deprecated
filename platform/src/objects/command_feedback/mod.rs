mod imp;

use gtk::glib;

glib::wrapper! {
    pub struct CommandFeedbackObject(ObjectSubclass<imp::CommandFeedbackObject>);
}
