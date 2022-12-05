use std::{collections::HashMap, sync::Mutex};

use lazy_static::lazy_static;

pub struct Command {
    command: &'static str,
    comment: String,
    action: String,
}

impl Command {
    pub fn new(command: &'static str, comment: String, action: String) -> Self {
        Command {
            command,
            comment,
            action,
        }
    }

    pub fn dynamic_feedback(&self, _input: &str) -> Vec<String> {
        let feedbacks = vec![];
        feedbacks
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

    #[allow(dead_code)]
    fn register(self) {
        if let Ok(mut guard) = COMMANDS.lock() {
            guard.insert(self.command, self);
        }
    }
}

#[macro_export]
macro_rules! reg_command {
    () => {};
    ( $($x:expr),* ) => {
        {
            $(
                $x.register();
            )*
        }
     };
}

lazy_static! {
    pub static ref COMMANDS: Mutex<HashMap<&'static str, Command>> = Mutex::new(HashMap::new());
}