use env_logger::{Builder, Target};
use gtk::gdk::Display;
use gtk::{prelude::*, CssProvider, StyleContext, STYLE_PROVIDER_PRIORITY_APPLICATION, HeaderBar};
use gtk::{Application, ApplicationWindow};
use log::info;

const APP_ID: &str = "termio.community";

/// Application entry of Termio Community.
fn main() {
    // Set the enviroment variable.
    std::env::set_var("GSK_RENDERER", "cairo");

    // Initialize log system.
    let mut builder = Builder::from_default_env();
    builder.target(Target::Stdout);
    builder.init();

    // Create a new application.
    let app = Application::builder().application_id(APP_ID).build();

    // Load css style sheet
    app.connect_startup(|_| load_css());

    // Connect to "activate" signal of `app`.
    app.connect_activate(build_ui);

    // Run the application
    app.run();
}

fn load_css() {
    let provider = CssProvider::new();
    provider.load_from_data(include_bytes!("style.css"));

    if let Some(display) = &Display::default() {
        StyleContext::add_provider_for_display(display, &provider, STYLE_PROVIDER_PRIORITY_APPLICATION);
    }
}

fn build_ui(app: &Application) {
    let titlebar = HeaderBar::builder().build();

    // Create main window
    let window = ApplicationWindow::builder()
        .application(app)
        .default_width(1280)
        .default_height(800)
        .title("")
        .titlebar(&titlebar)
        .build();

    // Present the window
    window.present();
    info!("Startup application termio-community success.");
}
