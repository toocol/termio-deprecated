#![allow(dead_code)]

use crate::{
    network::Transport,
    statesync::{CompleteTerminal, UserStream},
};
pub struct STMClient {
    ip: String,
    port: String,
    key: String,

    network: Transport,
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
        }
    }

    pub fn tick(&self) {
        
    }
}
