#![allow(dead_code)]
use std::{
    collections::{HashMap},
    rc::Rc,
};
use tmui::prelude::{KeyboardModifier, KeyCode};

#[cfg(target_os = "windows")]
static CTRL_MODIFIER: KeyboardModifier = KeyboardModifier::ControlModifier;
#[cfg(target_os = "macos")]
static CTRL_MODIFIER: KeyboardModifier = KeyboardModifier::MetaModifier;

lazy_static::lazy_static! {
    static ref DEFAULT_TRANSLATOR_TEXT: &'static [u8] = {
        "keyboard \"Fallback Key Translator\"\n
key Tab : \"\\t\"".as_bytes()
    };
}

#[inline]
fn one_or_zero(value: bool) -> u8 {
    if value {
        1
    } else {
        0
    }
}

#[inline]
fn is_printable_char(ch: u8) -> bool {
    ch >= 32 && ch < 127
}

#[inline]
fn is_xdigit(ch: u8) -> bool {
    (ch >= b'0' && ch <= b'9') || (ch >= b'A' && ch <= b'F') || (ch >= b'a' && ch <= b'f')
}

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
impl From<u8> for State {
    fn from(value: u8) -> Self {
        match value {
            0 => Self::NoState,
            1 => Self::NewLineState,
            2 => Self::AnsiState,
            4 => Self::CursorKeysState,
            8 => Self::AlternateScreenState,
            16 => Self::AnyModifierState,
            32 => Self::ApplicationKeypadState,
            _ => Self::Combination(value),
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

    is_null: bool,
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
            is_null: true,
        }
    }

    /// Returns true if this entry is null.
    /// This is true for newly constructed entries which have no properties set.
    #[inline]
    pub fn is_null(&self) -> bool {
        self.is_null
    }

    /// Returns the commands associated with this entry
    #[inline]
    pub fn command(&self) -> Command {
        self.command
    }

    /// Sets the command associated with this entry.
    #[inline]
    pub fn set_command(&mut self, command: Command) {
        self.command = command
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
        let expand_wild_cards = expand_wild_cards.is_some();
        let modifiers = if modifiers.is_some() {
            modifiers.unwrap()
        } else {
            KeyboardModifier::NoModifier
        };
        let mut expand_text = self.text.clone();

        if expand_wild_cards {
            let mut modifier_value = 1u8;
            modifier_value += one_or_zero(modifiers.has(KeyboardModifier::ShiftModifier));
            modifier_value += one_or_zero(modifiers.has(KeyboardModifier::AltModifier)) << 1;
            modifier_value += one_or_zero(modifiers.has(CTRL_MODIFIER)) << 2;

            for i in 0..self.text.len() {
                if expand_text[i] == b'*' {
                    expand_text[i] = b'0' + modifier_value
                }
            }
        }
        expand_text
    }

    /// Sets the character sequence associated with this entry.
    #[inline]
    pub fn set_text(&mut self, text: Vec<u8>) {
        self.text = self.unescape(text)
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
        let mut result = self.text(expand_wild_cards, modifiers);
        let mut i = 0usize;

        loop {
            if i >= result.len() {
                break;
            }

            let ch = result[i];
            let replacement = match ch {
                8 => b'b',
                9 => b't',
                10 => b'n',
                13 => b'r',
                12 => b'f',
                27 => b'E',
                //any character which is not printable is replaced by an equivalent
                // \xhh escape sequence (where 'hh' are the corresponding hex digits)
                _ => {
                    if is_printable_char(ch) {
                        0
                    } else {
                        b'x'
                    }
                }
            };

            if replacement == b'x' {
                let hex_str = hex::encode([ch]);
                let hex = hex_str.as_bytes();
                result[i] = b'\\';
                result.insert(i + 1, b'x');
                result.insert(i + 2, hex[0]);
                result.insert(i + 3, hex[1]);
            } else if replacement != 0 {
                result.remove(i);
                result.insert(i, b'\\');
                result.insert(i + 1, replacement);
            }
            i += 1;
        }

        result
    }

    /// Returns the character code ( from the Qt::Key enum ) associated with this entry.
    #[inline]
    pub fn key_code(&self) -> i32 {
        self.key_code
    }

    /// Sets the character code associated with this entry.
    #[inline]
    pub fn set_key_code(&mut self, key_code: i32) {
        self.key_code = key_code
    }

    /// Returns a bitwise-OR of the enabled keyboard modifiers associated with
    /// this entry. If a modifier is set in modifierMask() but not in
    /// modifiers(), this means that the entry only matches when that modifier is not pressed.
    ///
    /// If a modifier is not set in modifierMask() then the entry matches whether
    /// the modifier is pressed or not.
    #[inline]
    pub fn modifiers(&self) -> KeyboardModifier {
        self.modifiers
    }

    /// Returns the keyboard modifiers which are valid in this entry. See modifiers().
    #[inline]
    pub fn modifier_mask(&self) -> KeyboardModifier {
        self.modifier_mask
    }

    /// Set the modifiers.
    #[inline]
    pub fn set_modifiers(&mut self, modifier: KeyboardModifier) {
        self.modifiers = modifier
    }

    /// Set the modifier mask.
    #[inline]
    pub fn set_modifier_mask(&mut self, modifier_mask: KeyboardModifier) {
        self.modifier_mask = modifier_mask
    }

    /// Returns a bitwise-OR of the enabled state flags associated with this
    /// entry. If flag is set in stateMask() but not in state(), this means that
    /// the entry only matches when the terminal is NOT in that state.
    ///
    /// If a state is not set in stateMask() then the entry matches whether the terminal is in that state or not.
    #[inline]
    pub fn state(&self) -> State {
        self.state
    }

    /// Returns the state flags which are valid in this entry.  See state()
    #[inline]
    pub fn state_mask(&self) -> State {
        self.state_mask
    }

    /// Set the state.
    #[inline]
    pub fn set_state(&mut self, state: State) {
        self.state = state
    }

    /// Set the state mask.
    #[inline]
    pub fn set_state_mask(&mut self, mask: State) {
        self.state_mask = mask
    }

    /// Returns this entry's conditions ( ie. its key code, modifier and state criteria ) as a string.
    pub fn condition_to_string(&mut self) -> String {
        let mut result = KeyCode::from(self.key_code as u32).to_string();

        self.insert_modifier(&mut result, KeyboardModifier::ShiftModifier);
        self.insert_modifier(&mut result, KeyboardModifier::ControlModifier);
        self.insert_modifier(&mut result, KeyboardModifier::AltModifier);
        self.insert_modifier(&mut result, KeyboardModifier::MetaModifier);
        self.insert_modifier(&mut result, KeyboardModifier::KeypadModifier);

        self.insert_state(&mut result, State::AlternateScreenState);
        self.insert_state(&mut result, State::NewLineState);
        self.insert_state(&mut result, State::AlternateScreenState);
        self.insert_state(&mut result, State::AlternateScreenState);
        self.insert_state(&mut result, State::AlternateScreenState);
        self.insert_state(&mut result, State::AlternateScreenState);

        result
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
        if !self.text.is_empty() {
            String::from_utf8(self.escaped_text(expand_wild_cards, modifiers))
                .expect("Parse `text` to `String` failed.")
        } else if self.command == Command::EraseCommand {
            "Erase".to_string()
        } else if self.command == Command::ScrollPageUpCommand {
            "ScrollPageUpCommand".to_string()
        } else if self.command == Command::ScrollPageDownCommand {
            "ScrollPageDownCommand".to_string()
        } else if self.command == Command::ScrollLineUpCommand {
            "ScrollLineUp".to_string()
        } else if self.command == Command::ScrollLineDownCommand {
            "ScrollLineDown".to_string()
        } else if self.command == Command::ScrollLockCommand {
            "ScrollLock".to_string()
        } else if self.command == Command::ScrollUpToTopCommand {
            "ScrollUpToTop".to_string()
        } else if self.command == Command::ScrollDownToBottomCommand {
            "ScrollDownToBottom".to_string()
        } else {
            String::new()
        }
    }

    ///Returns true if this entry matches the given key sequence, specified
    /// as a combination of @p keyCode , @p modifiers and @p state.
    #[allow(unused_mut)]
    pub fn matches(&self, key_code: i32, modifiers: KeyboardModifier, flags: State) -> bool {
        let mut modifiers = modifiers;
        let mut flags = flags;
        #[cfg(target_os = "macos")]
        {
            // On Mac, arrow keys are considered part of keypad. Ignore that.
            modifiers = KeyboardModifier::from(
                modifiers.as_u32() & !KeyboardModifier::KeypadModifier.as_u32(),
            )
        }

        if self.key_code != key_code {
            return false;
        }
        if modifiers.as_u32() & self.modifier_mask.as_u32()
            != self.modifiers.as_u32() & self.modifier_mask.as_u32()
        {
            return false;
        }

        // if modifiers is non-zero, the 'any modifier' state is implicit
        if modifiers.as_u32() & !KeyboardModifier::KeypadModifier.as_u32() != 0 {
            flags = State::from(flags.as_u8() | State::AnyModifierState.as_u8());
        }

        if flags.as_u8() & self.state_mask.as_u8() != self.state.as_u8() & self.state_mask.as_u8() {
            return false;
        }

        let any_modifiers_set = modifiers != KeyboardModifier::NoModifier
            && modifiers != KeyboardModifier::KeypadModifier;
        let want_any_modifier = self.state.as_u8() & State::AnyModifierState.as_u8() > 0;
        if self.state_mask.as_u8() & State::AnyModifierState.as_u8() > 0 {
            if want_any_modifier != any_modifiers_set {
                return false;
            }
        }
        true
    }

    fn insert_modifier(&mut self, item: &mut String, modifier: KeyboardModifier) {
        if modifier.as_u32() & self.modifier_mask.as_u32() == 0 {
            return;
        }

        if modifier.as_u32() & self.modifiers.as_u32() > 0 {
            item.push('+');
        } else {
            item.push('-');
        }

        if modifier == KeyboardModifier::ShiftModifier {
            item.push_str("Shift")
        } else if modifier == KeyboardModifier::ControlModifier {
            item.push_str("Ctrl")
        } else if modifier == KeyboardModifier::AltModifier {
            item.push_str("Alt")
        } else if modifier == KeyboardModifier::MetaModifier {
            item.push_str("Meta")
        } else if modifier == KeyboardModifier::KeypadModifier {
            item.push_str("KeyPad");
        }
    }

    fn insert_state(&mut self, item: &mut String, state: State) {
        if state.as_u8() & self.state_mask.as_u8() == 0 {
            return;
        }

        if state.as_u8() & self.state.as_u8() > 0 {
            item.push('+');
        } else {
            item.push('-');
        }

        if state == State::AlternateScreenState {
            item.push_str("AppScreen")
        } else if state == State::NewLineState {
            item.push_str("NewLine")
        } else if state == State::AnsiState {
            item.push_str("Ansi")
        } else if state == State::CursorKeysState {
            item.push_str("AppCursorKeys")
        } else if state == State::AnyModifierState {
            item.push_str("AnyModifier")
        } else if state == State::ApplicationKeypadState {
            item.push_str("AppKaypad")
        }
    }

    fn unescape(&self, text: Vec<u8>) -> Vec<u8> {
        let mut result = text;
        let mut i = 9usize;
        loop {
            if i >= result.len() - 1 {
                break;
            }

            let ch = result[i];
            if ch == b'\\' {
                let mut replacement = 0u8;
                let mut erase_char = 1;
                let mut escaped_char = true;
                match result[i + 1] {
                    b'b' => replacement = 8,
                    b't' => replacement = 9,
                    b'n' => replacement = 10,
                    b'f' => replacement = 12,
                    b'r' => replacement = 13,
                    b'E' => replacement = 27,
                    b'x' => {
                        let mut hex_digits = [0u8; 2];
                        if i < result.len() - 2 && is_xdigit(result[i + 2]) {
                            hex_digits[0] = result[i + 2];
                        }
                        if i < result.len() - 3 && is_xdigit(result[i + 3]) {
                            hex_digits[1] = result[i + 3];
                        }
                        let char_val =
                            format!("{}{}", hex_digits[0] as char, hex_digits[1] as char);
                        let hex_byte = hex::decode(char_val).unwrap()[0];
                        replacement = hex_byte;
                        erase_char = 3;
                    }
                    _ => escaped_char = false,
                }

                if escaped_char {
                    result[i] = replacement;
                    for _ in 0..erase_char {
                        result.remove(i + 1);
                    }
                }
            }
            i += 1;
        }

        result
    }
}

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

/// Manages the keyboard translations available for use by terminal sessions
/// and loads the list of available keyboard translations.
///
/// The keyboard translations themselves are not loaded until they are
/// first requested via a call to find_translator()
pub struct KeyboardTranslatorManager {
    translators: HashMap<String, Rc<Box<KeyboardTranslator>>>,
    have_load_all: bool,
}

impl KeyboardTranslatorManager {
    pub fn new() -> Self {
        Self {
            translators: HashMap::new(),
            have_load_all: false,
        }
    }

    /// Adds a new translator.  If a translator with the same name already exists,
    /// it will be replaced by the new translator.
    pub fn add_translator(&mut self, translator: KeyboardTranslator) {
        todo!()
    }

    /// Deletes a translator.  Returns true on successful deletion or false otherwise.
    pub fn delete_translator(&mut self, name: String) -> bool {
        todo!()
    }

    /// Returns the default translator.
    pub fn default_translator(&self) -> KeyboardTranslator {
        todo!()
    }

    /// Returns the keyboard translator with the given name or 0 if no translator
    /// with that name exists.
    ///
    /// The first time that a translator with a particular name is requested,
    /// the on-disk .keyboard file is loaded and parsed.
    pub fn find_translator(&self, name: String) -> Rc<Box<KeyboardTranslator>> {
        todo!()
    }

    /// Returns a list of the names of available keyboard translators.
    ///
    /// The first time this is called, a search for available translators is started.
    pub fn all_translators(&self) -> Vec<String> {
        todo!()
    }

    /// Locate the avaliable translators
    fn find_translators(&self) {
        todo!()
    }

    // Load the translator.
    fn load_translator(&self, name: String) -> KeyboardTranslator {
        todo!()
    }

    fn save_translator(&self, translator: KeyboardTranslator) -> bool {
        todo!()
    }

    fn find_translator_path(&self, name: String) -> String {
        todo!()
    }
}

#[cfg(test)]
mod tests {
    #[test]
    fn test() {
        let mut list = vec![1, 2, 3, 4, 5, 6, 7, 8];
        let mut i = 0usize;
        loop {
            println!("{}", list.len());
            if i >= list.len() {
                break;
            }

            let val = list[i];

            if val == 3 {
                list.remove(i);
                list.insert(i, 20);
                list.insert(i + 1, 21);
            }
            if val == 8 {
                list.insert(i + 1, 30);
            }
            i += 1;
        }
        println!("{:?}", list)
    }
}
