use gtk::glib::subclass::InitializingObject;
use gtk::subclass::prelude::ObjectSubclass;

use gtk::subclass::prelude::*;
use gtk::{glib, CompositeTemplate, ScrolledWindow, Inhibit};
use gtk::prelude::*;

use crate::ui::terminal::NativeTerminalEmulator;
use log::debug;

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
    fn size_allocate(&self, width: i32, height: i32, baseline: i32) {
        self.parent_size_allocate(width, height, baseline);

        let allocation = self.instance().imp().gtk_scrolled_window.allocation();
        self.native_terminal_emulator.resize(allocation.width(), allocation.height());
        debug!("Window size allocate! w: {}, h: {}, baseline: {}", allocation.width(), allocation.height(), baseline);
    }
}

impl WindowImpl for TermioCommunityWindow {
    fn close_request(&self) -> Inhibit {
        self.parent_close_request();
        debug!("Application closed.");
        Inhibit(false)
    }
}

impl ApplicationWindowImpl for TermioCommunityWindow {}
