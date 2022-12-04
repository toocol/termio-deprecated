mod window;

use gtk::gdk::Display;
use gtk::Application;
use gtk::{
    gio, prelude::*, CssProvider, Settings, StyleContext, STYLE_PROVIDER_PRIORITY_APPLICATION,
};
use log::info;
use platform::{load_font, APP_COMMUNITY_ID, ACTION_TOGGLE_LEFT_AREA};
use utilities::Asset;
use window::TermioCommunityWindow;

//  gsettings set org.gtk.Settings.Debug enable-inspector-keybinding false
/// Application entry of Termio Community.
fn main() {
    // Set the enviroment variable.
    std::env::set_var("GSK_RENDERER", "cairo");

    // Initialize log system.
    initialize_log_system();

    // Load ui layout resources.
    gio::resources_register_include!("temio_community.gresource")
        .expect("Initialize application failed: failed to register resources.");

    // Load font file.
    load_font!(
        "Font-Awesome-6-Brands-Regular-400.otf",
        "Font-Awesome-6-Free-Regular-400.otf",
        "Font-Awesome-6-Free-Solid-900.otf",
        "SegMDL2.ttf",
        "Segoe-Fluent-Icons.ttf"
    );

    // Create a new application.
    let app = Application::builder()
        .application_id(APP_COMMUNITY_ID)
        .build();

    app.connect_startup(|app| {
        // Load css style sheet
        load_css();
        // Setup shortcut
        setup_shortcuts(app);
    });

    // Connect to "activate" signal of `app`.
    app.connect_activate(|app| {
        prelude_settings();
        build_ui(app);
    });

    // Run the application
    app.run();
}

fn initialize_log_system() {
    log4rs::init_file("src/resources/log4rs.yaml", Default::default()).unwrap();
}

fn load_css() {
    let provider = CssProvider::new();
    let data = Asset::get("style.css")
        .expect("Get embed asset `style.css` failed.")
        .data;
    provider.load_from_data(data.as_ref());

    if let Some(display) = &Display::default() {
        StyleContext::add_provider_for_display(
            display,
            &provider,
            STYLE_PROVIDER_PRIORITY_APPLICATION,
        );
    }
}

fn prelude_settings() {
    if let Some(settings) = Settings::default() {
        settings.set_gtk_titlebar_right_click(Some("none"));
    }
}

fn build_ui(app: &Application) {
    let window = TermioCommunityWindow::new(app);
    window.show();
    info!("Startup application termio-community success.");
}

fn setup_shortcuts(app: &Application) {
    app.set_accels_for_action(
        ACTION_TOGGLE_LEFT_AREA.activate().as_str(),
        &["<Alt>1"],
    )
}
