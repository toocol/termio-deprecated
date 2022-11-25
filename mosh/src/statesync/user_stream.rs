#![allow(dead_code)]
use super::UserEvent;

#[derive(Debug, PartialEq, Eq)]
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

    pub fn diff_from(&self, _existing: &UserStream) -> Vec<u8> {
        todo!()
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
