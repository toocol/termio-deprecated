use core::SessionCredential;
use std::fs::File;

use gtk::{
    glib::{self, once_cell::sync::OnceCell, subclass::InitializingObject},
    prelude::*,
    subclass::prelude::{ObjectSubclass, *},
    Button, CompositeTemplate, Inhibit, Overlay, Paned, ScrolledWindow,
};

use crate::{
    ui::{
        terminal::NativeTerminalEmulator, NewSessionDialog, SessionCredentialManagementTree,
        UIHolder, UI,
    },
    util::data_path,
};
use log::debug;
use platform::SessionCredentialObject;

#[derive(Default, CompositeTemplate)]
#[template(resource = "/com/toocol/termio/community/window.ui")]
pub struct TermioCommunityWindow {
    #[template_child]
    pub workspace_paned: TemplateChild<Paned>,
    #[template_child]
    pub create_new_session_button: TemplateChild<Button>,
    #[template_child]
    pub session_credential_management: TemplateChild<SessionCredentialManagementTree>,
    #[template_child]
    pub workspace_terminal_scrolled_window: TemplateChild<ScrolledWindow>,
    #[template_child]
    pub terminal_emulator_overlay: TemplateChild<Overlay>,
    #[template_child]
    pub native_terminal_emulator: TemplateChild<NativeTerminalEmulator>,

    pub new_session_dialog: OnceCell<NewSessionDialog>,
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
        obj.resotre_data();

        self.workspace_paned.set_shrink_start_child(false);
        self.workspace_paned.set_shrink_end_child(false);
        self.workspace_paned.set_resize_start_child(true);
        self.workspace_paned.set_resize_end_child(true);
        self.workspace_paned.set_position(230);

        self.session_credential_management
            .setup_callbacks(obj.as_ref());

        UI._holder
            .set(UIHolder::create(
                self.instance().clone(),
                self.session_credential_management.clone(),
            ))
            .expect("`_holder` of UI should noly set once.");
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
        // debug!("Window size allocate! w: {}, h: {}, baseline: {}", allocation.width(), allocation.height(), baseline);
    }
}

impl WindowImpl for TermioCommunityWindow {
    fn close_request(&self) -> Inhibit {
        debug!("Application closed.");

        let backup_data: Vec<SessionCredential> = self
            .session_credential_management
            .session_credentials()
            .iter()
            .map(SessionCredentialObject::to_session_credetial)
            .collect();
        // Save state to file
        let file = File::create(data_path(".credential")).expect("Could not create json file.");
        serde_json::to_writer(file, &backup_data).expect("Could not write data to json file.");

        self.parent_close_request()
    }
}

impl ApplicationWindowImpl for TermioCommunityWindow {}
