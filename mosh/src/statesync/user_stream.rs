#![allow(dead_code)]
use protobuf::Message;

use super::{UserEvent, UserEventType};
use crate::proto::userinput;

const KEYSTROKE_NUMBER: u32 = 2;
const RESIZE_NUMBER: u32 = 3;

#[derive(Debug, PartialEq, Eq, Clone)]
pub struct UserStream {
    actions: Vec<UserEvent>,
}

impl UserStream {
    pub fn new() -> Self {
        UserStream { actions: vec![] }
    }

    pub fn subtract(&mut self, prefix: &UserStream) {
        if self == prefix {
            self.actions.clear();
            return;
        }

        for next in prefix.actions.iter() {
            let mut peek = self.actions.first();
            if let Some(peek) = peek.take() {
                if peek == next {
                    self.actions.remove(0);
                }
            }
        }
    }

    pub fn diff_from(&self, existing: &UserStream) -> Vec<u8> {
        let mut iter = existing.actions.iter();
        let mut my_it = self.actions.iter();
        while let Some(_) = iter.next() {
            my_it.next();
        }

        let mut user_message = userinput::UserMessage::new();

        while let Some(next) = my_it.next() {
            match next.event_type() {
                UserEventType::ResizeType => {
                    let resize_event = next.to_resize();
                    let mut instruction = userinput::Instruction::new();
                    let mut resize = userinput::ResizeMessage::new();
                    resize.set_width(resize_event.width());
                    resize.set_height(resize_event.height());
                    instruction.mut_unknown_fields().add_length_delimited(
                        RESIZE_NUMBER,
                        resize
                            .write_to_bytes()
                            .expect("`ResizeMessage` write to bytes failed."),
                    );
                    user_message.instruction.push(instruction);
                }
                UserEventType::UserByteType => {
                    let user_bytes_event = next.to_user_bytes();
                    let mut instruction = userinput::Instruction::new();
                    let mut keystroke = userinput::Keystroke::new();
                    keystroke.set_keys(user_bytes_event.bytes());
                    instruction.mut_unknown_fields().add_length_delimited(
                        KEYSTROKE_NUMBER,
                        keystroke
                            .write_to_bytes()
                            .expect("`Keystoke` write to bytes failed."),
                    );
                    user_message.instruction.push(instruction);
                }
            }
        }

        user_message
            .write_to_bytes()
            .expect("`UserMessage` write to bytes failed.")
    }

    pub fn push_back(&mut self, event: UserEvent) {
        self.actions.push(event);
    }

    pub fn copy(&self) -> UserStream {
        UserStream {
            actions: self.actions.clone(),
        }
    }

    pub fn action_size(&self) -> usize {
        self.actions.len()
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_protobuf() {
        let mut user_message = userinput::UserMessage::new();
        let mut instruction = userinput::Instruction::new();
        let mut keystroke = userinput::Keystroke::new();
        keystroke.set_keys(vec![0u8; 3]);
        let mut resize = userinput::ResizeMessage::new();
        resize.set_width(100);
        resize.set_height(100);
        instruction.mut_unknown_fields().add_length_delimited(
            KEYSTROKE_NUMBER,
            keystroke
                .write_to_bytes()
                .expect("`Keystoke` write to bytes failed."),
        );
        instruction.mut_unknown_fields().add_length_delimited(
            RESIZE_NUMBER,
            resize
                .write_to_bytes()
                .expect("`ResizeMessage` write to bytes failed."),
        );
        user_message.instruction.push(instruction);

        let bytes = user_message
            .write_to_bytes()
            .expect("`UserMessage` write to bytes failed.");

        let user_message = userinput::UserMessage::parse_from_bytes(&bytes)
            .expect("`UserMessage` parse from bytes failed.");
        user_message.instruction.iter().for_each(|ins| {
            let resize = userinput::exts::resize
                .get(ins)
                .expect("`Instruction` get ResizeMessage failed.");
            assert_eq!(100, resize.width());
            assert_eq!(100, resize.height());

            let keystroke = userinput::exts::keystroke
                .get(ins)
                .expect("`Instruction` get Keystroke failed.");
            assert_eq!(3, keystroke.keys().len());
        });
    }
}
