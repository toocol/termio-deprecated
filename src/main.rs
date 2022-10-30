mod ui;

use gtk::gdk::Display;
use gtk::Application;
use gtk::{
    gio, prelude::*, CssProvider, Settings, StyleContext, STYLE_PROVIDER_PRIORITY_APPLICATION,
};
use log::info;

use crate::ui::TermioCommunityWindow;

const APP_ID: &str = "termio.community";

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

    // Create a new application.
    let app = Application::builder().application_id(APP_ID).build();

    // Load css style sheet
    app.connect_startup(|_| load_css());

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
    provider.load_from_data(include_bytes!("resources/style.css"));

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
    window.present();
    info!("Startup application termio-community success.");
}
