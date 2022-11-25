#![allow(dead_code)]
use std::collections::HashMap;

const ACK_BUFFER: usize = 32;

pub struct CompleteTerminal {
    output_queue: Vec<Vec<u8>>,
    acked: HashMap<u64, Vec<u8>>,

    echo_ack: i64,
}

impl CompleteTerminal {
    pub fn new() -> Self {
        CompleteTerminal {
            output_queue: vec![],
            acked: HashMap::new(),
            echo_ack: -1,
        }
    }

    pub fn apply_string(&self, _diff: Vec<u8>, _ack_num: u64) {
        todo!()
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
        // TODO: Show in terminal
    }
}
