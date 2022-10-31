use gtk::glib::subclass::InitializingObject;
use gtk::subclass::prelude::ObjectSubclass;

use gtk::subclass::prelude::*;
use gtk::{glib, CompositeTemplate, ScrolledWindow};
use gtk::prelude::*;

use crate::ui::terminal::NativeTerminalEmulator;

#[derive(Default, CompositeTemplate)]
#[template(resource = "/com/toocol/termio/community/window.ui")]
pub struct TermioCommunityWindow {
    #[template_child]
    pub gtk_scrolled_window: TemplateChild<ScrolledWindow>,
    #[template_child]
    pub native_terminal_emulator: TemplateChild<NativeTerminalEmulator>,
}

#[glib::object_subclass]
impl ObjectSubclass for TermioCommunityWindow {
    const NAME: &'static str = "TermioCommunityWindow";

    type Type = super::TermioCommunityWindow;

    type ParentType = gtk::ApplicationWindow;

    fn class_init(klass: &mut Self::Class) {
        klass.bind_template();
    }

    fn instance_init(obj: &InitializingObject<Self>) {
        obj.init_template();
    }
}

impl ObjectImpl for TermioCommunityWindow {}

impl WidgetImpl for TermioCommunityWindow {
    fn realize(&self) {
        self.parent_realize();

        let allocation = self.instance().imp().gtk_scrolled_window.allocation();
        println!("Window realize! w:{}, h:{}", allocation.width(), allocation.height());
    }

    fn snapshot(&self, snapshot: &gtk::Snapshot) {
        self.parent_snapshot(snapshot);
        
        let allocation = self.instance().imp().gtk_scrolled_window.allocation();
        println!("Window snapshot! w:{}, h:{}", allocation.width(), allocation.height());
    }
}

impl WindowImpl for TermioCommunityWindow {}

impl ApplicationWindowImpl for TermioCommunityWindow {}
