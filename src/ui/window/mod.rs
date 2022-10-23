mod imp;

use gtk::glib::Object;
use gtk::{gio, glib, Application};

glib::wrapper! {
    pub struct TermioCommunityWindow(ObjectSubclass<imp::TermioCommunityWindow>)
        @extends gtk::ApplicationWindow, gtk::Window, gtk::Widget,
        @implements gio::ActionGroup, gio::ActionMap, gtk::Accessible, gtk::Buildable,
                    gtk::ConstraintTarget, gtk::Native, gtk::Root, gtk::ShortcutManager;
}

impl TermioCommunityWindow {
    pub fn new(app: &Application) -> Self {
        Object::new(&[("application", app)])
    }
}
