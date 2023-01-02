#![allow(dead_code)]

#[repr(u8)]
#[derive(Debug, Default, PartialEq, Eq, Clone, Copy)]
pub enum ScrollBarPosition {
    #[default]
    NoScrollBar = 0,
    ScrollBarLeft,
    ScrollBarRight,
}
impl From<u8> for ScrollBarPosition {
    fn from(x: u8) -> Self {
        match x {
            0 => Self::NoScrollBar,
            1 => Self::ScrollBarLeft,
            2 => Self::ScrollBarRight,
            _ => unimplemented!(),
        }
    }
}

#[repr(u8)]
#[derive(Debug, Default, PartialEq, Eq, Clone, Copy)]
pub enum TripleClickMode {
    #[default]
    SelectWholeLine = 0,
    SelectForwardsFromCursor
}
impl From<u8> for TripleClickMode {
    fn from(x: u8) -> Self {
        match x {
            0 => Self::SelectWholeLine,
            1 => Self::SelectForwardsFromCursor,
            _ => unimplemented!(),
        }
    }
}

#[repr(u8)]
#[derive(Debug, Default, PartialEq, Eq, Clone, Copy)]
pub enum MotionAfterPasting {
    #[default]
    NoMoveScreenWindow = 0,
    MoveStartScreenWindow,
    MoveEndScreenWindow,
}
impl From<u8> for MotionAfterPasting {
    fn from(x: u8) -> Self {
        match x {
            0 => Self::NoMoveScreenWindow,
            1 => Self::MoveStartScreenWindow,
            2 => Self::MoveEndScreenWindow,
            _ => unimplemented!(),
        }
    }
}

#[repr(u8)]
#[derive(Debug, Default, PartialEq, Eq, Clone, Copy)]
pub enum KeyboardCursorShape {
    #[default]
    BlockCursor = 0,
    UnderlineCursor,
    IBeamCursor
}
impl From<u8> for KeyboardCursorShape {
    fn from(x: u8) -> Self {
        match x {
            0 => Self::BlockCursor,
            1 => Self::UnderlineCursor,
            2 => Self::IBeamCursor,
            _ => unimplemented!(),
        }
    }
}

#[repr(u8)]
#[derive(Debug, Default, PartialEq, Eq, Clone, Copy)]
pub enum BackgroundMode {
    #[default]
    None = 0,
    Stretch,
    Zoom,
    Fit,
    Center,
}
impl From<u8> for BackgroundMode {
    fn from(x: u8) -> Self {
        match x {
            0 => Self::None,
            1 => Self::Stretch,
            2 => Self::Zoom,
            3 => Self::Fit,
            4 => Self::Center,
            _ => unimplemented!(),
        }
    }
}

#[repr(u8)]
#[derive(Debug, Default, PartialEq, Eq, Clone, Copy)]
pub enum BellMode {
    #[default]
    SystemBeepBell = 0,
    NotifyBell,
    VisualBell,
    NoBell,
}
impl From<u8> for BellMode {
    fn from(x: u8) -> Self {
        match x {
            0 => Self::SystemBeepBell,
            1 => Self::NotifyBell,
            2 => Self::VisualBell,
            3 => Self::NoBell,
            _ => unimplemented!(),
        }
    }
}

#[repr(u8)]
#[derive(Debug, Default, PartialEq, Eq, Clone, Copy)]
pub enum DragState {
    #[default]
    DiNone = 0,
    DiPending,
    DiDragging,
}
impl From<u8> for DragState {
    fn from(x: u8) -> Self {
        match x {
            0 => Self::DiNone,
            1 => Self::DiPending,
            2 => Self::DiDragging,
            _ => unimplemented!(),
        }
    }
}