#![allow(dead_code)]
use std::{cell::RefCell, collections::HashMap, rc::Rc};

use protobuf::Message;

use crate::{proto::hostinput, terminal::Emulator};

const ACK_BUFFER: usize = 32;

#[derive(Debug, PartialEq, Eq, Clone)]
pub struct CompleteTerminal {
    terminal: Rc<RefCell<Emulator>>,
    output_queue: Vec<Vec<u8>>,
    acked: HashMap<u64, Vec<u8>>,

    echo_ack: i64,
}

impl CompleteTerminal {
    pub fn new(emulator: Rc<RefCell<Emulator>>) -> Self {
        CompleteTerminal {
            terminal: emulator,
            output_queue: vec![],
            acked: HashMap::new(),
            echo_ack: -1,
        }
    }

    pub fn apply_string(&mut self, diff: &[u8], ack_num: u64) {
        let input = hostinput::HostMessage::parse_from_bytes(&diff)
            .expect("`HostMessage` parse from bytes failed.");
        for ins in input.instruction.iter() {
            if let Some(host_string) = hostinput::exts::hostbytes.get(ins) {
                let host_string = host_string.hoststring();
                let mut host_vec = vec![0u8; host_string.len()];
                host_vec[..].copy_from_slice(host_string);
                self.act(host_vec, ack_num);
            }
            if let Some(_) = hostinput::exts::resize.get(ins) {}
            if let Some(echo_ack) = hostinput::exts::echoack.get(ins) {
                let echo_ack_num = echo_ack.echo_ack_num();
                if echo_ack_num as i64 == self.echo_ack {
                    return;
                }
                self.echo_ack = echo_ack_num as i64;
            }
        }
    }

    pub fn act(&mut self, bytes: Vec<u8>, ack_num: u64) {
        if self.acked.contains_key(&ack_num) && ack_num != 1 {
            if let Some(val) = self.acked.get(&ack_num) {
                if val.len() >= bytes.len() {
                    return;
                }
            }
        }

        if self.acked.len() >= ACK_BUFFER {
            let mut cnt = 0;
            self.acked.retain(|_, _| {
                if cnt == ACK_BUFFER / 2 {
                    return true;
                }
                cnt += 1;
                false
            });
        }

        self.acked.insert(ack_num, bytes.clone());
        self.terminal
            .borrow()
            .print(String::from_utf8(bytes).unwrap().as_str());
    }
}
