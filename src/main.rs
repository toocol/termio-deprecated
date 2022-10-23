mod ui;

use chrono::Local;
use env_logger::Target;
use gtk::gdk::Display;
use gtk::Application;
use gtk::{
    gio, prelude::*, CssProvider, Settings, StyleContext, STYLE_PROVIDER_PRIORITY_APPLICATION,
};
use log::info;

use crate::ui::TermioCommunityWindow;
use std::io::Write;

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
    let env = env_logger::Env::default().filter_or(env_logger::DEFAULT_FILTER_ENV, "trace");
    env_logger::Builder::from_env(env)
        .format(|buf, record| {
            writeln!(
                buf,
                "{} {} [{}] {}",
                Local::now().format("%Y-%m-%d %H:%M:%S"),
                record.level(),
                record.module_path().unwrap_or("<unnamed>"),
                &record.args()
            )
        })
        .target(Target::Stdout)
        .init();
}

fn load_css() {
    let provider = CssProvider::new();
    provider.load_from_data(include_bytes!("style.css"));

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
    TermioCommunityWindow::new(app).present();
    info!("Startup application termio-community success.");
}
