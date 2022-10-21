use env_logger::{Builder, Target};
use gtk::prelude::*;
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

    // Connect to "activate" signal of `app`.
    app.connect_activate(build_ui);

    // Run the application
    app.run();
}

fn build_ui(app: &Application) {
    // Create main window
    let window = ApplicationWindow::builder()
        .application(app)
        .default_width(1280)
        .default_height(800)
        .title("Termio Community")
        .build();

    // Present the window
    window.present();
    info!("Startup application termio-community success.");
}
