#![allow(dead_code)]
use std::{collections::HashMap, rc::Rc};

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
    NoState,
    /// Indicates that terminal is in new line state.
    NewLineState,
    /// Indicates that the terminal is in 'Ansi' mode.
    AnsiState,
    /// Indicates that the terminal is in cursor key state.
    CursorKeysState,
    /// Indicates that the alternate screen ( typically used by interactive
    /// programs such as screen or vim ) is active
    AlternateScreenState,
    /// Indicates that any of the modifier keys is active.
    AnyModifierState,
    /// Indicates that the numpad is in application mode.
    ApplicationKeypadState,
    /// State combinations.
    Combination(u8),
}
impl State {
    pub fn or(&self, other: State) -> State {
        let one = self.as_u8();
        let other = other.as_u8();
        Self::Combination(one | other)
    }

    pub fn has(&self, has: State) -> bool {
        match self {
            Self::Combination(state) => state & has.as_u8() > 0,
            _ => *self == has,
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

    /// Returns true if this entry is null.
    /// This is true for newly constructed entries which have no properties set.
    pub fn is_null(&self) -> bool {
        todo!()
    }

    /// Returns the commands associated with this entry
    pub fn command(&self) -> &Command {
        todo!()
    }

    /// Sets the command associated with this entry.
    pub fn set_command(&mut self, command: Command) {
        todo!()
    }

    /// Returns the character sequence associated with this entry, optionally
    /// replacing wildcard '*' characters with numbers to indicate the keyboard
    /// modifiers being pressed.
    ///
    ///
    /// @param expandWildCards Specifies whether wild cards (occurrences of the
    /// '*' character) in the entry should be replaced with a number to indicate
    // the modifier keys being pressed.
    ///
    /// @param modifiers The keyboard modifiers being pressed.
    pub fn text(
        &self,
        expand_wild_cards: Option<bool>,
        modifiers: Option<KeyboardModifier>,
    ) -> Vec<u8> {
        todo!()
    }

    /// Sets the character sequence associated with this entry.
    pub fn set_text(&mut self, text: Vec<u8>) {
        todo!()
    }

    ///  Returns the character sequence associated with this entry,
    /// with any non-printable characters replaced with escape sequences.
    ///
    /// eg. \\E for Escape, \\t for tab, \\n for new line.
    ///
    /// @param expandWildCards See text()
    /// @param modifiers See text()
    pub fn escaped_text(
        &self,
        expand_wild_cards: Option<bool>,
        modifiers: Option<KeyboardModifier>,
    ) -> Vec<u8> {
        todo!()
    }

    /// Returns the character code ( from the Qt::Key enum ) associated with this entry.
    pub fn key_code(&self) -> i32 {
        todo!()
    }

    /// Sets the character code associated with this entry.
    pub fn set_key_code(&mut self, key_code: i32) {
        todo!()
    }

    /// Returns a bitwise-OR of the enabled keyboard modifiers associated with
    /// this entry. If a modifier is set in modifierMask() but not in
    /// modifiers(), this means that the entry only matches when that modifier is not pressed.
    ///
    /// If a modifier is not set in modifierMask() then the entry matches whether
    /// the modifier is pressed or not.
    pub fn modifiers(&self) -> KeyboardModifier {
        todo!()
    }

    /// Returns the keyboard modifiers which are valid in this entry. See modifiers().
    pub fn modifier_mask(&self) -> KeyboardModifier {
        todo!()
    }

    /// Set the modifiers.
    pub fn set_modifiers(&mut self, modifier: KeyboardModifier) {
        todo!()
    }

    /// Set the modifier mask.
    pub fn set_modifier_mask(&self, modifier_mask: KeyboardModifier) {
        todo!()
    }

    /// Returns a bitwise-OR of the enabled state flags associated with this
    /// entry. If flag is set in stateMask() but not in state(), this means that
    /// the entry only matches when the terminal is NOT in that state.
    ///
    /// If a state is not set in stateMask() then the entry matches whether the terminal is in that state or not.
    pub fn state(&self) -> State {
        todo!()
    }

    /// Returns the state flags which are valid in this entry.  See state()
    pub fn state_mask(&self) -> State {
        todo!()
    }

    /// Set the state.
    pub fn set_state(&mut self, state: State) {
        todo!()
    }

    /// Set the state mask.
    pub fn set_state_mask(&mut self, mask: State) {
        todo!()
    }

    /// Returns this entry's conditions ( ie. its key code, modifier and state criteria ) as a string.
    pub fn condition_to_string(&self) -> String {
        todo!()
    }

    /// Returns this entry's result ( ie. its command or character sequence ) as a string.
    ///
    /// @param expandWildCards See text()
    /// @param modifiers See text()
    pub fn result_to_string(
        &self,
        expand_wild_cards: Option<bool>,
        modifiers: Option<KeyboardModifier>,
    ) -> String {
        todo!()
    }

    ///Returns true if this entry matches the given key sequence, specified
    /// as a combination of @p keyCode , @p modifiers and @p state.
    pub fn matches(&self, key_code: i32, modifiers: KeyboardModifier, flags: String) -> bool {
        todo!()
    }

    fn insert_modifier(&mut self, item: String, modifier: i32) {
        todo!()
    }

    fn insert_state(&mut self, item: String, state: i32) {
        todo!()
    }

    fn unescape(&self, text: Vec<u8>) -> Vec<u8> {
        todo!()
    }
}

static CTRL_MODIFIER: KeyboardModifier = KeyboardModifier::ControlModifier;

pub struct KeyboardTranslator {
    entries: HashMap<i32, Vec<Rc<Entry>>>,
    name: String,
    description: String,
}

impl KeyboardTranslator {
    /// Constructs a new keyboard translator with the given @p name.
    pub fn new(name: String) -> Self {
        Self {
            entries: HashMap::new(),
            name: name,
            description: String::new(),
        }
    }

    /// Returns the name of this keyboard translator.
    pub fn name(&self) -> &str {
        todo!()
    }

    /// Sets the name of this keyboard translator.
    pub fn set_name(&mut self, name: String) {
        todo!()
    }

    /// Returns the descriptive name of this keyboard translator.
    pub fn description(&self) -> &str {
        todo!()
    }

    /// Sets the descriptive name of this keyboard translator.
    pub fn set_description(&mut self, description: String) {
        todo!()
    }

    /// Looks for an entry in this keyboard translator which matches the given key code, keyboard modifiers and state flags.
    ///
    /// Returns the matching entry if found or a null Entry otherwise ( ie. entry.isNull() will return true )
    ///
    /// @param keyCode A key code from the Qt::Key enum
    /// @param modifiers A combination of modifiers
    /// @param state Optional flags which specify the current state of the terminal
    pub fn find_entry(&self, modifiers: KeyboardModifier, state: Option<State>) -> Rc<Entry> {
        todo!()
    }

    /// Adds an entry to this keyboard translator's table.  Entries can be looked
    /// up according to their key sequence using findEntry()
    pub fn add_entry(&self, entry: Entry) {
        todo!()
    }

    /// Replaces an entry in the translator.  If the @p existing entry is null,
    /// then this is equivalent to calling addEntry(@p replacement)
    pub fn replace_entry(&mut self, existing: Entry, replacement: Entry) {
        todo!()
    }

    /// Removes an entry from the table.
    pub fn remove_entry(&mut self, entry: Entry) {
        todo!()
    }

    /// Returns a list of all entries in the translator.
    pub fn entries(&self) -> Vec<Rc<Entry>> {
        todo!()
    }
}

#[repr(C)]
pub enum TokenType {
    TitleKeyword,
    TitleText,
    KeyKeyword,
    KeySequence,
    Command,
    OutputText,
}
pub struct Token {
    type_: TokenType,
    text: String,
}

/// Parses the contents of a Keyboard Translator (.keytab) file and
/// returns the entries found in it.
pub struct KeyboardTranslatorReader {
    description: String,
    next_entry: Option<Entry>,
    has_next: bool,
}

impl KeyboardTranslatorReader {
    /////////////////////// public function
    pub fn new() -> Self {
        Self {
            description: String::new(),
            next_entry: None,
            has_next: false,
        }
    }

    pub fn create_entry(condition: String, result: String) -> Entry {
        todo!()
    }

    /// Returns the description text.
    pub fn description(&self) -> &str {
        todo!()
    }

    /// Returns true if there is another entry in the source stream.
    pub fn has_next_entry(&self) -> bool {
        todo!()
    }

    /// Returns the next entry found in the source stream
    pub fn next_entry(&self) -> Entry {
        todo!()
    }

    ///  Returns true if an error occurred whilst parsing the input or false if no error occurred.
    pub fn parse_error(&self) -> bool {
        todo!()
    }

    /////////////////////// private function
    fn parse_as_modifier(item: String, modifier: KeyboardModifier) -> bool {
        todo!()
    }

    fn parse_as_state_flag(item: String, state: State) -> bool {
        todo!()
    }

    fn parse_as_key_code(item: String, key_code: i32) -> bool {
        todo!()
    }

    fn parse_as_command(text: String, command: Command) -> bool {
        todo!()
    }

    fn tokenize(&self, text: String) -> Vec<Token> {
        todo!()
    }

    fn read_next(&self) {
        todo!()
    }

}

pub struct KeyboardTranslatorManager {

}
