use core::SessionCredential;
use std::fs::File;

use gtk::{
    glib::{self, clone, once_cell::sync::OnceCell, subclass::InitializingObject},
    prelude::*,
    subclass::prelude::{ObjectSubclass, *},
    CompositeTemplate, HeaderBar, Inhibit, Overlay, Paned, ScrolledWindow, Stack, Separator,
};

use log::debug;
use platform::{
    termio::data_path, ActivityBar, ActivityBarItem, EditionMark, IconButton,
    NativeTerminalEmulator, NewSessionDialog, SessionCredentialManagementTree,
    SessionCredentialObject, Termio, WidgetTitleBar,
};

#[derive(Default, CompositeTemplate)]
#[template(resource = "/com/toocol/termio/community/window.ui")]
pub struct TermioCommunityWindow {
    ///////////////// Main layout
    #[template_child]
    pub global_overlay: TemplateChild<Overlay>,
    #[template_child]
    pub workbench_box: TemplateChild<gtk::Box>,
    #[template_child]
    pub workspace_box: TemplateChild<gtk::Box>,
    #[template_child]
    pub workspace_paned: TemplateChild<Paned>,

    ///////////////// Header bar
    #[template_child]
    pub window_header_bar: TemplateChild<HeaderBar>,
    #[template_child]
    pub toggle_left_area_button: TemplateChild<IconButton>,
    #[template_child]
    pub toggle_bottom_area_button: TemplateChild<IconButton>,

    ///////////////// Activity bar
    #[template_child]
    pub workspace_activity_bar: TemplateChild<ActivityBar>,

    #[template_child]
    pub workspace_activity_bar_top_box: TemplateChild<gtk::Box>,
    #[template_child]
    pub toggle_session_management_item: TemplateChild<ActivityBarItem>,
    #[template_child]
    pub toggle_plugin_extensions_item: TemplateChild<ActivityBarItem>,

    #[template_child]
    pub workspace_activity_bar_bottom_box: TemplateChild<gtk::Box>,
    #[template_child]
    pub toggle_setting_item: TemplateChild<ActivityBarItem>,

    ///////////////// Left side bar
    #[template_child]
    pub left_side_bar_seperator: TemplateChild<Separator>,
    #[template_child]
    pub workspace_left_side_bar: TemplateChild<Stack>,

    ///////////////// Session credential management
    #[template_child]
    pub session_management_wrap_box: TemplateChild<gtk::Box>,
    #[template_child]
    pub session_management_title_bar: TemplateChild<WidgetTitleBar>,
    #[template_child]
    pub session_credential_management: TemplateChild<SessionCredentialManagementTree>,

    ///////////////// Native teminal emulator
    #[template_child]
    pub workspace_terminal_scrolled_window: TemplateChild<ScrolledWindow>,
    #[template_child]
    pub native_terminal_emulator: TemplateChild<NativeTerminalEmulator>,

    ///////////////// Bottom status bar
    #[template_child]
    pub bottom_status_bar: TemplateChild<gtk::Box>,
    #[template_child]
    pub bottom_status_bar_left_box: TemplateChild<gtk::Box>,
    #[template_child]
    pub bottom_status_bar_right_box: TemplateChild<gtk::Box>,
    #[template_child]
    pub edition_mark: TemplateChild<EditionMark>,

    pub termio: OnceCell<Termio>,
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
        self.termio
            .set(Termio::TermioCommunity)
            .expect("`termio` of TermioCommunityWindow can only set once.");

        let obj = self.instance();
        obj.initialize();
        obj.setup_actions();
        obj.resotre_data();

        self.workspace_left_side_bar.set_width_request(50);

        self.workspace_paned.set_shrink_start_child(false);
        self.workspace_paned.set_shrink_end_child(false);
        self.workspace_paned.set_resize_start_child(true);
        self.workspace_paned.set_resize_end_child(true);
        self.workspace_paned.set_position(250);

        let terminal_window = &*self.workspace_terminal_scrolled_window;
        let terminal_emulator = &*self.native_terminal_emulator;
        self.workspace_paned.connect_position_notify(
            clone!(@weak terminal_window, @weak terminal_emulator => move |_| {
                let allocation = terminal_window.allocation();
                terminal_emulator.resize(allocation.width(), allocation.height());
            }),
        );

        self.instance().set_titlebar(Some(&*self.window_header_bar));
    }

    fn dispose(&self) {
        self.workspace_activity_bar_top_box.unparent();
        self.workspace_activity_bar_bottom_box.unparent();
    }
}

impl WidgetImpl for TermioCommunityWindow {
    fn size_allocate(&self, width: i32, height: i32, baseline: i32) {
        self.parent_size_allocate(width, height, baseline);

        let allocation = self.workspace_terminal_scrolled_window.allocation();
        self.native_terminal_emulator
            .resize(allocation.width(), allocation.height());
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
        let file = File::create(data_path(
            ".credential",
            self.termio
                .get()
                .expect("`termio` of TermioCommunityWindow should set first before use."),
        ))
        .expect("Could not create json file.");
        serde_json::to_writer(file, &backup_data).expect("Could not write data to json file.");

        self.parent_close_request()
    }
}

impl ApplicationWindowImpl for TermioCommunityWindow {}
