use gtk::{Application, ApplicationWindow, Button, glib, Orientation};
use gtk::prelude::*;

const APP_ID: &str = "termio.community";

fn main() {
    // Create a new application
    let app = Application::builder().application_id(APP_ID).build();

    // Connect to "activate" signal of `app`
    app.connect_activate(build_ui);

    // Run the application
    app.run();
}

fn build_ui(app: &Application) {

}
