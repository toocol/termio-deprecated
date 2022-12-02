#![allow(dead_code)]

use super::{Connection, Fragment, FragmentAssembly, TransportSender, MOSH_PROTOCOL_VERSION};
use log::error;

pub struct Transport {
    /* the underlying, encrypted network connection */
    connection: Connection,
    /* sender side */
    sender: TransportSender,

    fragments: FragmentAssembly,
}

impl Transport {
    pub fn tick(&mut self) {
        self.sender.tick();
    }

    pub fn receive_packet(&mut self, bytes: Vec<u8>) {
        let bytes = self.connection.recv_one(bytes);
        let fragment = Fragment::from_bytes(&bytes);
        if self.fragments.add_fragment(fragment) {
            if let Some(inst) = self.fragments.get_assembly() {
                if inst.protocol_version() != MOSH_PROTOCOL_VERSION {
                    error!(
                        "Mosh protocol version mismatch, accept = {}, get = {}",
                        MOSH_PROTOCOL_VERSION,
                        inst.protocol_version()
                    );
                    return;
                }
            }
        }
    }
}
