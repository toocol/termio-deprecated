use gtk::glib::subclass::InitializingObject;
use gtk::subclass::prelude::ObjectSubclass;

use gtk::subclass::prelude::*;
use gtk::{glib, CompositeTemplate};
use gtk::prelude::*;

use crate::ui::terminal::NativeTerminalEmulator;

#[derive(Default, CompositeTemplate)]
#[template(resource = "/com/toocol/termio/community/window.ui")]
pub struct TermioCommunityWindow {
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

impl WidgetImpl for TermioCommunityWindow {}

impl WindowImpl for TermioCommunityWindow {}

impl ApplicationWindowImpl for TermioCommunityWindow {}
