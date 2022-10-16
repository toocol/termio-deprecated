use gtk::{Application, ApplicationWindow};
use gtk::prelude::*;

const APP_ID: &str = "termio.community";

///
/// Application entry of Termio Community.
///
/// Run with environment:
/// ```
/// GSK_RENDERER=cairo
/// ```
fn main() {
    // Create a new application
    let app = Application::builder().application_id(APP_ID).build();

    // Connect to "activate" signal of `app`
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
}
