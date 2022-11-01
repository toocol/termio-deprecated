use gtk::glib::subclass::InitializingObject;
use gtk::subclass::prelude::ObjectSubclass;

use gtk::subclass::prelude::*;
use gtk::{glib, CompositeTemplate, ScrolledWindow};
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
    fn realize(&self) {
        self.parent_realize();

        let allocation = self.instance().imp().gtk_scrolled_window.allocation();
        debug!("Window realize! w:{}, h:{}", allocation.width(), allocation.height());
    }

    fn size_allocate(&self, width: i32, height: i32, baseline: i32) {
        self.parent_size_allocate(width, height, baseline);
        debug!("Window size allocate! w: {}, h: {}, baseline: {}", width, height, baseline);

        let window_allocation = self.gtk_scrolled_window.allocation();
        self.native_terminal_emulator.connect_native(window_allocation.width(), window_allocation.height());
    }

    fn snapshot(&self, snapshot: &gtk::Snapshot) {
        self.parent_snapshot(snapshot);
        let window_allocation = self.gtk_scrolled_window.allocation();
        let terminal_allocation = self.native_terminal_emulator.allocation();
        debug!("Window snapshot! w:{}, h:{}", window_allocation.width(), window_allocation.height());
        debug!("Terminal snapshot! w:{}, h:{}", terminal_allocation.width(), terminal_allocation.height());
    }
}

impl WindowImpl for TermioCommunityWindow {}

impl ApplicationWindowImpl for TermioCommunityWindow {}
