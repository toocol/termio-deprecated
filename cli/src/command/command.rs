use std::{collections::HashMap, sync::Mutex};

use lazy_static::lazy_static;

#[derive(Debug)]
pub struct CommandParamPattern {
    pattern: &'static str,
    comment: String,
}
impl CommandParamPattern {
    pub fn new(pattern: &'static str, comment: String) -> Self {
        CommandParamPattern { pattern, comment }
    }

    pub fn pattern(&self) -> &'static str {
        self.pattern
    }

    pub fn comment(&self) -> &str {
        self.comment.as_str()
    }
}

pub enum InputCommandState {
    Less, Equal, More
}

#[derive(Debug)]
pub struct Command {
    command: &'static str,
    comment: String,
    action: String,
    param_pattern: Option<CommandParamPattern>,
    shortcuts: Option<Vec<&'static str>>,
}
impl Command {
    pub fn new(
        command: &'static str,
        comment: String,
        action: String,
        param_pattern: Option<CommandParamPattern>,
        shortcuts: Option<Vec<&'static str>>,
    ) -> Self {
        Command {
            command,
            comment,
            action,
            param_pattern,
            shortcuts,
        }
    }

    pub fn command(&self) -> &'static str {
        self.command
    }

    pub fn comment(&self) -> &str {
        self.comment.as_str()
    }

    pub fn action(&self) -> &str {
        self.action.as_str()
    }

    pub fn param_pattern(&self) -> &Option<CommandParamPattern> {
        &self.param_pattern
    }

    pub fn shortcuts(&self) -> &Option<Vec<&'static str>> {
        &self.shortcuts
    }

    #[allow(dead_code)]
    pub fn register(self) {
        if let Ok(mut guard) = COMMANDS.lock() {
            guard.insert(self.command, self);
        }
    }
}

lazy_static! {
    pub static ref COMMANDS: Mutex<HashMap<&'static str, Command>> = Mutex::new(HashMap::new());
}
