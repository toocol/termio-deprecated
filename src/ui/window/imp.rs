use core::SessionCredential;
use std::fs::File;

use gtk::glib::subclass::InitializingObject;
use gtk::subclass::prelude::ObjectSubclass;

use gtk::prelude::*;
use gtk::subclass::prelude::*;
use gtk::{glib, CompositeTemplate, Inhibit, ScrolledWindow};

use crate::ui::SessionCredentialManagementTree;
use crate::ui::terminal::NativeTerminalEmulator;
use crate::util::data_path;
use log::debug;

#[derive(Default, CompositeTemplate)]
#[template(resource = "/com/toocol/termio/community/window.ui")]
pub struct TermioCommunityWindow {
    #[template_child]
    pub session_credential_management: TemplateChild<SessionCredentialManagementTree>,
    #[template_child]
    pub workspace_terminal_scrolled_window: TemplateChild<ScrolledWindow>,
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

impl ObjectImpl for TermioCommunityWindow {
    fn constructed(&self) {
        self.parent_constructed();
        
        let obj = self.instance();
        obj.initialize();
        obj.setup_actions();
    }
}

impl WidgetImpl for TermioCommunityWindow {
    fn size_allocate(&self, width: i32, height: i32, baseline: i32) {
        self.parent_size_allocate(width, height, baseline);

        let allocation = self
            .instance()
            .imp()
            .workspace_terminal_scrolled_window
            .allocation();
        self.native_terminal_emulator
            .resize(allocation.width(), allocation.height());
        debug!(
            "Window size allocate! w: {}, h: {}, baseline: {}",
            allocation.width(),
            allocation.height(),
            baseline
        );
    }
}

impl WindowImpl for TermioCommunityWindow {
    fn close_request(&self) -> Inhibit {
        debug!("Application closed.");
        self.session_credential_management
            .session_credentials();
        
        let backup_data: Vec<SessionCredential> = vec![];
                // Save state to file
        let file = File::create(data_path(".credential")).expect("Could not create json file.");
        serde_json::to_writer(file, &backup_data).expect("Could not write data to json file.");

        self.parent_close_request()
    }
}

impl ApplicationWindowImpl for TermioCommunityWindow {}
