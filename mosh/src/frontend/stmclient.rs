#![allow(dead_code)]

use std::{rc::Rc, cell::RefCell};

use crate::{
    network::Transport,
    statesync::{CompleteTerminal, UserStream}, terminal::Emulator,
};
use super::OverlayManager;

pub struct STMClient {
    ip: String,
    port: String,
    key: String,

    network: Transport,
    overlay: OverlayManager,
}

impl STMClient {
    pub fn new(ip: String, port: String, key: String) -> Self {
        let emulator = Rc::new(RefCell::new(Emulator::new()));
        let initial_state = UserStream::new();
        let initial_remote = CompleteTerminal::new(emulator.clone());
        let transport = Transport::new(
            initial_state,
            initial_remote,
            ip.as_str(),
            port.as_str(),
            key.as_str(),
        );

        STMClient {
            ip,
            port,
            key,
            network: transport,
            overlay: OverlayManager::new(emulator),
        }
    }

    pub fn main(&mut self) {
        loop {
            if let Some(resize_event) = self.overlay.terminal_size_aware() {
                self.network.push_back_event(resize_event);
            }

            if let Some(user_bytes)  = self.overlay.read() {
                self.network.push_back_event(user_bytes);
            }

            if let Some(recvd) = self.network.recv() {
                self.network.receive_packet(recvd);
            }

            self.tick();
        }
    }

    pub fn tick(&mut self) {
        self.network.tick();
    }
}
