#![allow(dead_code)]
use crate::statesync::UserEvent;
use std::{
    borrow::BorrowMut,
    io::stdin,
    sync::{
        mpsc::{channel, Receiver},
        Mutex,
    },
    thread,
};
use terminal_size::terminal_size;

static RECEIVER: Mutex<Option<Receiver<Vec<u8>>>> = Mutex::new(None);

#[derive(Debug, PartialEq, Eq, Clone)]
pub struct Emulator {
    pub width: i32,
    pub height: i32,
}

impl Emulator {
    pub fn new() -> Self {
        let (sender, receiver) = channel::<Vec<u8>>();
        if let Ok(mut guard) = RECEIVER.lock() {
            guard.borrow_mut().replace(receiver);
        }

        thread::spawn(move || {
            let mut input = String::new();
            stdin()
                .read_line(&mut input)
                .ok()
                .expect("Terminal emualtor failed to read line.");
            sender
                .send(input.into_bytes())
                .expect("Terminal emulator send input failed.");
        });

        Self {
            width: 0,
            height: 0,
        }
    }

    pub fn terminal_size_aware(&mut self) -> Option<UserEvent> {
        let size = terminal_size();
        if let Some((w, h)) = size {
            let w = w.0 as i32;
            let h = h.0 as i32;
            if self.width != w || self.height != h {
                self.width = w;
                self.height = h;
                Some(UserEvent::new_resize(w, h))
            } else {
                None
            }
        } else {
            None
        }
    }

    pub fn read(&self) -> Option<UserEvent> {
        if let Ok(guard) = RECEIVER.lock() {
            if let Ok(input) = guard.as_ref().unwrap().try_recv() {
                return Some(UserEvent::new_user_bytes(input))
            } else {
                return None
            }
        }
        None
    }

    pub fn print(&self, output: &str) {
        print!("{}", output);
    }
}
