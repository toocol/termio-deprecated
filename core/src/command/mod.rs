use std::{collections::HashMap, sync::Mutex};

use lazy_static::lazy_static;

pub struct Command {
    pub command: &'static str,
    pub comment: &'static str,
    pub action: String,
}

impl Command {
    pub fn new(command: &'static str, comment: &'static str, action: String) -> Self {
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
