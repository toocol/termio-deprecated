#![allow(dead_code)]
#[repr(u32)]
pub enum GtkMouseButton {
    All = 0,
    Left,
    Middle,
    Right,
    NoButton,
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
            GtkMouseButton::Left => QtMouseButton::PrimaryButton as i32,
            GtkMouseButton::Middle => QtMouseButton::MiddleButton as i32,
            GtkMouseButton::Right => QtMouseButton::SecondaryButton as i32,
            _ => unreachable!(),
        }
    }

    pub fn from_code(code: i32) -> Self {
        match code {
            0 => Self::NoButton,
            1 => Self::PrimaryButton,
            2 => Self::SecondaryButton,
            4 => Self::MiddleButton,
            _ => unimplemented!()
        }
    }

    pub fn to_gtk_button(&self) -> GtkMouseButton {
        match self {
            Self::NoButton => GtkMouseButton::NoButton,
            Self::PrimaryButton => GtkMouseButton::Left,
            Self::SecondaryButton => GtkMouseButton::Right,
            Self::MiddleButton => GtkMouseButton::Middle,
        }
    }
}
