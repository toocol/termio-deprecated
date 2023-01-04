use super::{is_letter_or_number, Command, Entry, State, KEY_REGEX, TITLE_REGEX};
use crate::tools::text_stream::LineReader;
use log::warn;
use tmui::tlib::namespace::{KeyboardModifier, KeyCode};

#[repr(C)]
#[derive(Debug, PartialEq, Eq, Clone, Copy)]
pub enum TokenType {
    TitleKeyword,
    TitleText,
    KeyKeyword,
    KeySequence,
    Command,
    OutputText,
}

#[derive(Debug, PartialEq, Eq, Clone)]
pub struct Token {
    type_: TokenType,
    text: String,
}
impl Token {
    fn new(type_: TokenType, text: String) -> Self {
        Self {
            type_: type_,
            text: text,
        }
    }
}

/// Parses the contents of a Keyboard Translator (.keytab) file and
/// returns the entries found in it.
pub struct KeyboardTranslatorReader {
    source: LineReader,
    description: String,
    next_entry: Option<Entry>,
    has_next: bool,
}

impl KeyboardTranslatorReader {
    //////////////////////////////////////////////////////////////////////// public function
    /// each line of the keyboard translation file is one of:
    ///
    /// - keyboard "name"
    /// - key KeySequence : "characters"
    /// - key KeySequence : CommandName
    ///
    /// KeySequence begins with the name of the key ( taken from the Qt::Key enum )
    /// and is followed by the keyboard modifiers and state flags ( with + or - in
    /// front of each modifier or flag to indicate whether it is required ).  All
    /// keyboard modifiers and flags are optional, if a particular modifier or state
    /// is not specified it is assumed not to be a part of the sequence.  The key sequence may contain whitespace
    ///
    /// eg:  "key Up+Shift : scrollLineUp"
    ///      "key Next-Shift : "\E[6~"
    ///
    /// (lines containing only whitespace are ignored, parseLine assumes that comments have already been removed)
    pub fn new(source: String) -> Self {
        let mut reader = Self {
            source: LineReader::new(source),
            description: String::new(),
            next_entry: None,
            has_next: false,
        };
        while let Some(line) = reader.source.next() {
            if !reader.description.is_empty() {
                break;
            }
            let tokens = reader.tokenize(line);
            if !tokens.is_empty() && tokens.first().unwrap().type_ == TokenType::TitleKeyword {
                reader.description = tokens[1].text.clone();
                break;
            }
        }

        // Read first entry, if any
        reader.read_next();

        reader
    }

    pub fn create_entry(condition: String, result: String) -> Entry {
        let mut entry_string = "keyboard \"temporary\"\nkey ".to_string();
        entry_string.push_str(&condition);
        entry_string.push_str(" : ");

        // if 'result' is the name of a command then the entry result will be that
        // command, otherwise the result will be treated as a string to echo when the
        // key sequence specified by 'condition' is pressed
        let mut command = Command::NoCommand;
        if Self::parse_as_command(&result, &mut command) {
            entry_string.push_str(&result);
        } else {
            let mut str = "\"".to_string();
            str.push_str(&result);
            str.push_str("\"");
            entry_string.push_str(&str);
        }

        let mut reader = Self::new(entry_string);
        let mut entry = Entry::new();
        if reader.has_next_entry() {
            entry = reader.next_entry();
        }
        entry
    }

    /// Returns the description text.
    pub fn description(&self) -> &str {
        &self.description
    }

    /// Returns true if there is another entry in the source stream.
    pub fn has_next_entry(&self) -> bool {
        self.has_next
    }

    /// Returns the next entry found in the source stream
    pub fn next_entry(&mut self) -> Entry {
        assert!(self.has_next);
        let entry = self.next_entry.take().unwrap();
        self.read_next();
        entry
    }

    //////////////////////////////////////////////////////////////////////// private function
    fn parse_as_modifier(item: &String, modifier: &mut KeyboardModifier) -> bool {
        if item.to_lowercase() == "shift" {
            *modifier = KeyboardModifier::ShiftModifier
        } else if item.to_lowercase() == "ctrl" || item.to_lowercase() == "control" {
            *modifier = KeyboardModifier::ControlModifier
        } else if item.to_lowercase() == "alt" {
            *modifier = KeyboardModifier::AltModifier
        } else if item.to_lowercase() == "meta" {
            *modifier = KeyboardModifier::MetaModifier
        } else if item.to_lowercase() == "keypad" {
            *modifier = KeyboardModifier::KeypadModifier
        } else {
            return false;
        }

        true
    }

    fn parse_as_state_flag(item: &String, state: &mut State) -> bool {
        if item.to_lowercase() == "appcukeys" || item.to_lowercase() == "appcursorkeys" {
            *state = State::CursorKeysState
        } else if item.to_lowercase() == "ansi" {
            *state = State::AnsiState
        } else if item.to_lowercase() == "newline" {
            *state = State::NewLineState
        } else if item.to_lowercase() == "appscreen" {
            *state = State::AlternateScreenState
        } else if item.to_lowercase() == "anymod" || item.to_lowercase() == "anymodifier" {
            *state = State::AnyModifierState
        } else if item.to_lowercase() == "appkeypad" {
            *state = State::ApplicationKeypadState
        } else {
            return false;
        }

        true
    }

    fn parse_as_key_code(item: &String, key_code: &mut u32) -> bool {
        let code = KeyCode::from(item);
        if !(code == KeyCode::Unknown) {
            let code: u32 = code.into();
            *key_code = code;
        } else if item.to_lowercase() == "prior" {
            let code: u32 = KeyCode::KeyPageUp.into();
            *key_code = code;
        } else if item.to_lowercase() == "next" {
            let code: u32 = KeyCode::KeyPageDown.into();
            *key_code = code;
        } else {
            return false;
        }
        true
    }

    fn parse_as_command(text: &String, command: &mut Command) -> bool {
        if text.to_lowercase() == "erase" {
            *command = Command::EraseCommand
        } else if text.to_lowercase() == "scrollpageup" {
            *command = Command::ScrollPageUpCommand
        } else if text.to_lowercase() == "scrollpagedown" {
            *command = Command::ScrollPageDownCommand
        } else if text.to_lowercase() == "scrolllineup" {
            *command = Command::ScrollLineUpCommand
        } else if text.to_lowercase() == "scrolllinedown" {
            *command = Command::ScrollLineDownCommand
        } else if text.to_lowercase() == "scrolllock" {
            *command = Command::ScrollLockCommand
        } else if text.to_lowercase() == "scrolluptotop" {
            *command = Command::ScrollUpToTopCommand
        } else if text.to_lowercase() == "scrolldowntobottom" {
            *command = Command::ScrollDownToBottomCommand
        } else {
            return false;
        }

        true
    }

    fn tokenize(&self, text: &str) -> Vec<Token> {
        let mut text = text.trim().as_bytes().to_vec();
        let mut in_quotes = false;
        let mut comment_pos = -1;
        let mut i = text.len() as i32 - 1;
        loop {
            if i < 0 {
                break;
            }
            let ch = text[i as usize];
            if ch == b'"' {
                in_quotes = !in_quotes;
            } else if ch == b'#' && !in_quotes {
                comment_pos = i
            }

            i -= 1;
        }

        if comment_pos != -1 {
            let len = text.len();
            // Remove the comment conetent like: "# xxxxxxx"
            for _ in comment_pos..len as i32 {
                text.remove(comment_pos as usize);
            }
        }

        let text = String::from_utf8(text).unwrap();
        let mut list = vec![];
        if TITLE_REGEX.is_match(&text) {
            let title = TITLE_REGEX
                .captures(&text)
                .unwrap()
                .get(1)
                .unwrap()
                .as_str();
            let title_token = Token::new(TokenType::TitleKeyword, String::new());
            let text_token = Token::new(TokenType::TitleText, title.to_string());

            list.push(title_token);
            list.push(text_token);
        } else if KEY_REGEX.is_match(&text) {
            let caps = KEY_REGEX.captures(&text).unwrap();
            let key_token = Token::new(TokenType::KeyKeyword, String::new());
            let sequence = caps.get(1).unwrap().as_str();
            let sequence_token = Token::new(TokenType::KeySequence, sequence.to_string());

            list.push(key_token);
            list.push(sequence_token);

            if caps.get(3).is_none() {
                let command = caps.get(2).unwrap().as_str();
                let command_token = Token::new(TokenType::Command, command.to_string());
                list.push(command_token);
            } else {
                let output = caps.get(3).unwrap().as_str();
                let output_command = Token::new(TokenType::OutputText, output.to_string());
                list.push(output_command);
            }
        } else {
            warn!(
                "Line in keyboard layout file could not parse, line: {}",
                text
            )
        }

        list
    }

    fn read_next(&mut self) {
        while let Some(line) = self.source.next() {
            let tokens = self.tokenize(line);
            if !tokens.is_empty() && tokens.first().unwrap().type_ == TokenType::KeyKeyword {
                let mut flags = State::NoState;
                let mut flag_mask = State::NoState;
                let mut modifiers = KeyboardModifier::NoModifier;
                let mut modifier_mask = KeyboardModifier::NoModifier;
                let mut key_code = 0u32;

                self.decode_sequence(
                    &tokens[1].text,
                    &mut key_code,
                    &mut modifiers,
                    &mut modifier_mask,
                    &mut flags,
                    &mut flag_mask,
                );

                let mut command = Command::NoCommand;
                let mut text = vec![];

                if tokens[2].type_ == TokenType::OutputText {
                    text = tokens[2].text.as_bytes().to_vec();
                } else if tokens[2].type_ == TokenType::Command {
                    if !Self::parse_as_command(&tokens[2].text, &mut command) {
                        warn!("Command `{}` parse failed.", tokens[2].text);
                    }
                }

                let mut new_entry = Entry::new();
                new_entry.key_code = key_code;
                new_entry.state = flags;
                new_entry.state_mask = flag_mask;
                new_entry.modifiers = modifiers;
                new_entry.modifier_mask = modifier_mask;
                new_entry.text = text;
                new_entry.command = command;

                self.next_entry = Some(new_entry);
                self.has_next = true;
                return;
            }
        }
        self.has_next = false;
    }

    fn decode_sequence(
        &self,
        text: &str,
        key_code: &mut u32,
        modifiers: &mut KeyboardModifier,
        modifier_mask: &mut KeyboardModifier,
        flags: &mut State,
        flag_mask: &mut State,
    ) -> bool {
        let text = text.as_bytes();
        let mut end_of_item;
        let mut is_wanted = true;
        let mut buffer = String::new();

        let mut temp_modifiers = modifiers.clone();
        let mut temp_modifier_mask = modifier_mask.clone();
        let mut temp_flags = flags.clone();
        let mut temp_flag_mask = flag_mask.clone();

        for i in 0..text.len() {
            let ch = text[i];
            let is_first_letter = i == 0;
            let is_last_letter = i == text.len() - 1;
            end_of_item = true;

            if is_letter_or_number(ch) {
                end_of_item = false;
                buffer.push(ch as char);
            } else if is_first_letter {
                buffer.push(ch as char);
            }

            if (end_of_item || is_last_letter) && !buffer.is_empty() {
                let mut item_modifier = KeyboardModifier::NoModifier;
                let mut item_key_code = 0u32;
                let mut item_flag = State::NoState;

                if Self::parse_as_modifier(&buffer, &mut item_modifier) {
                    temp_modifier_mask = temp_modifier_mask.or(item_modifier);

                    if is_wanted {
                        temp_modifiers = temp_modifiers.or(item_modifier);
                    }
                } else if Self::parse_as_state_flag(&buffer, &mut item_flag) {
                    temp_flag_mask = temp_flag_mask.or(item_flag);

                    if is_wanted {
                        temp_flags = temp_flags.or(item_flag);
                    }
                } else if Self::parse_as_key_code(&buffer, &mut item_key_code) {
                    *key_code = item_key_code;
                } else {
                    warn!("Unable to parse key binding item: {}", buffer)
                }

                buffer.clear()
            }

            // check if this is a wanted / not-wanted flag and update the
            // state ready for the next item
            if ch == b'+' {
                is_wanted = true;
            } else if ch == b'-' {
                is_wanted = false;
            }
        }

        *modifiers = temp_modifiers;
        *modifier_mask = temp_modifier_mask;
        *flags = temp_flags;
        *flag_mask = temp_flag_mask;

        true
    }
}
