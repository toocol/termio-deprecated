#![allow(dead_code)]
#[repr(u32)]
pub enum GtkMouseButton {
    ALL = 0,
    LEFT,
    MIDDLE,
    RIGHT
}

#[repr(i32)]
pub enum QtMouseButton {
    NoButton = 0,
    PrimaryButton = 1,
    SecondaryButton = 2,
    MiddleButton = 4,
}

impl QtMouseButton {
    pub fn from_gtk_button(gtk_mouse_button: GtkMouseButton) -> i32 {
        match gtk_mouse_button {
            GtkMouseButton::LEFT => QtMouseButton::PrimaryButton as i32,
            GtkMouseButton::MIDDLE => QtMouseButton::MiddleButton as i32,
            GtkMouseButton::RIGHT => QtMouseButton::PrimaryButton as i32,
            _ => unreachable!()
        }
    }
}