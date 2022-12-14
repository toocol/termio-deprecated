use crate::{Command, InputCommandState, COMMANDS};

pub struct CommandFeedback {
    pub command: String,
    pub param: Option<String>,
    pub comment: String,
    pub shortcuts: Option<Vec<&'static str>>,
}

impl CommandFeedback {
    pub fn command_without_param(command: &Command) -> Self {
        CommandFeedback {
            command: command.command().to_string(),
            param: None,
            comment: command.comment().to_string(),
            shortcuts: command.shortcuts().clone(),
        }
    }

    pub fn command_with_param(command: &Command) -> Self {
        let command_param = command.param_pattern().as_ref().unwrap();
        CommandFeedback {
            command: command.command().to_string(),
            param: Some(command_param.pattern().to_string()),
            comment: command_param.comment().to_string(),
            shortcuts: None,
        }
    }
}

#[derive(Default)]
pub struct DynamicFeedback;
impl DynamicFeedback {
    pub fn new() -> Self {
        DynamicFeedback {}
    }

    pub fn dynamic_feedback(&self, input: &str) -> Vec<CommandFeedback> {
        let mut feedbacks = vec![];
        if input.len() == 0 {
            // TODO: Load recent used command's feedback.
        } else {
            if let Ok(guard) = COMMANDS.lock() {
                for (key, val) in guard.iter() {
                    if key.contains(input) || input.contains(key) {
                        let state = if key.len() > input.len() {
                            InputCommandState::Less
                        } else if key.len() == input.len() {
                            InputCommandState::Equal
                        } else {
                            InputCommandState::More
                        };
                        feedbacks.append(&mut self.generate_command_feedback(val, state));
                    }
                }
            }
        }
        feedbacks
    }

    fn generate_command_feedback(
        &self,
        command: &Command,
        origin: InputCommandState,
    ) -> Vec<CommandFeedback> {
        let mut feedbacks = vec![];
        match origin {
            InputCommandState::Less => {
                let origin_feedback = CommandFeedback::command_without_param(command);
                feedbacks.push(origin_feedback);
                if command.param_pattern().is_some() {
                    let with_param_feedback = CommandFeedback::command_with_param(command);
                    feedbacks.push(with_param_feedback);
                }
            }
            InputCommandState::Equal => {
                let origin_feedback = CommandFeedback::command_without_param(command);
                feedbacks.push(origin_feedback);
                if command.param_pattern().is_some() {
                    let with_param_feedback = CommandFeedback::command_with_param(command);
                    feedbacks.push(with_param_feedback);
                }
            }
            InputCommandState::More => {}
        }
        feedbacks
    }
}
