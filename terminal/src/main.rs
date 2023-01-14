use tmui::{application::Application, application_window::ApplicationWindow};

mod asset;
mod core;
mod emulation;
mod pty;
mod tools;

fn main() {
    log4rs::init_file("terminal/log4rs.yaml", Default::default()).unwrap();

    let app = Application::builder()
        .width(1280)
        .height(800)
        .title("Termio Terminal Emulator")
        .build();

    app.connect_activate(build_ui);

    app.run();
}

fn build_ui(_window: &ApplicationWindow) {
    println!("Hello Terminal");
}
