#![allow(dead_code)]
use tmui::prelude::KeyboardModifier;

/// A convertor which maps between key sequences pressed by the user and the
/// character strings which should be sent to the terminal and commands
/// which should be invoked when those character sequences are pressed.
///
/// Supports multiple keyboard translators, allowing the user to
/// specify the character sequences which are sent to the terminal when particular key sequences are pressed.
///
/// A key sequence is defined as a key code, associated keyboard modifiers (Shift,Ctrl,Alt,Meta etc.)
/// and state flags which indicate the state which the terminal must be in for the key sequence to apply.
#[repr(u8)]
#[derive(Debug, Default, PartialEq, Eq, Clone, Copy)]
pub enum State {
    /// Indicates that no special state is active.
    #[default]
    NoState = 0,
    /// Indicates that terminal is in new line state.
    NewLineState = 1,
    /// Indicates that the terminal is in 'Ansi' mode.
    AnsiState = 2,
    /// Indicates that the terminal is in cursor key state.
    CursorKeysState = 4,
    /// Indicates that the alternate screen ( typically used by interactive
    /// programs such as screen or vim ) is active
    AlternateScreenState = 8,
    /// Indicates that any of the modifier keys is active.
    AnyModifierState = 16,
    /// Indicates that the numpad is in application mode.
    ApplicationKeypadState = 32,
    /// State combinations.
    Combination(u8)
}
impl State {
    pub fn or(&self, other: State) -> State {
        let one = self.as_u8();
        let other = other.as_u8();
        Self::Combination(one | other)
    }

    pub fn has(&self, has: State) -> bool {
        match self {
            Self::Combination(state) => {
                state & has.as_u8() > 0
            }
            _ => *self == has
        }
    }

    pub fn as_u8(&self) -> u8 {
        match self {
            Self::NoState => 0,
            Self::NewLineState => 1,
            Self::AnsiState => 2,
            Self::CursorKeysState => 4,
            Self::AlternateScreenState => 8,
            Self::AnyModifierState => 16,
            Self::ApplicationKeypadState => 32,
            Self::Combination(state) => *state,
        }
    }
}
impl Into<u8> for State {
    fn into(self) -> u8 {
        match self {
            Self::NoState => 0,
            Self::NewLineState => 1,
            Self::AnsiState => 2,
            Self::CursorKeysState => 4,
            Self::AlternateScreenState => 8,
            Self::AnyModifierState => 16,
            Self::ApplicationKeypadState => 32,
            Self::Combination(state) => state,
        }
    }
}

/// This enum describes commands which are associated with particular key sequences.
#[repr(u16)]
#[derive(Debug, Default, PartialEq, Eq, Clone, Copy)]
pub enum Command {
    /// Indicates that no command is associated with this command sequence.
    #[default]
    NoCommand = 0,
    /// Send command.
    SendComand = 1,
    /// Scroll the terminal display up one page.
    ScrollPageUpCommand = 2,
    /// Scroll the terminal display down one page.
    ScrollPageDownCommand = 4,
    /// Scroll the terminal display up one line.
    ScrollLineUpCommand = 8,
    /// Scroll the terminal display down one line.
    ScrollLineDownCommand = 16,
    /// Toggles scroll lock mode.
    ScrollLockCommand = 32,
    /// Scroll the terminal display up to the start of history.
    ScrollUpToTopCommand = 64,
    /// Scroll the terminal display down to the end of history.
    ScrollDownToBottomCommand = 128,
    /// Echos the operating system specific erase character.
    EraseCommand = 256,
}
impl Into<u16> for Command {
    fn into(self) -> u16 {
        match self {
            Self::NoCommand => Self::NoCommand as u16,
            Self::SendComand => Self::SendComand as u16,
            Self::ScrollPageUpCommand => Self::ScrollPageUpCommand as u16,
            Self::ScrollPageDownCommand => Self::ScrollPageDownCommand as u16,
            Self::ScrollLineUpCommand => Self::ScrollLineUpCommand as u16,
            Self::ScrollLineDownCommand => Self::ScrollLineDownCommand as u16,
            Self::ScrollLockCommand => Self::ScrollLockCommand as u16,
            Self::ScrollUpToTopCommand => Self::ScrollUpToTopCommand as u16,
            Self::ScrollDownToBottomCommand => Self::ScrollDownToBottomCommand as u16,
            Self::EraseCommand => Self::EraseCommand as u16,
        }
    }
}

/// Represents an association between a key sequence pressed by the user
/// and the character sequence and commands associated with it for a particular KeyboardTranslator.
#[derive(Debug, PartialEq, Eq)]
pub struct Entry {
    key_code: i32,
    modifiers: KeyboardModifier,
    modifier_mask: KeyboardModifier,

    state: State,
    state_mask: State,

    command: Command,
    text: Vec<u8>,
}

impl Entry {
    pub fn new() -> Self {
        Self {
            key_code: 0,
            modifiers: KeyboardModifier::NoModifier,
            modifier_mask: KeyboardModifier::NoModifier,
            state: State::NoState,
            state_mask: State::NoState,
            command: Command::NoCommand,
            text: vec![],
        }
    }
}
