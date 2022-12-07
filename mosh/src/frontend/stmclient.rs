#![allow(dead_code)]

use crate::{
    network::Transport,
    statesync::{CompleteTerminal, UserStream},
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
        let initial_state = UserStream::new();
        let initial_remote = CompleteTerminal::new();
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
            overlay: OverlayManager::new(),
        }
    }

    pub fn main(&mut self) {
        loop {
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
