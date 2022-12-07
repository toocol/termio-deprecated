#![allow(dead_code)]

use super::{
    Connection, Fragment, FragmentAssembly, TimestampState, TransportSender, MOSH_PROTOCOL_VERSION,
};
use crate::statesync::{CompleteTerminal, UserStream, UserEvent};
use log::{error, warn};
use std::{cell::RefCell, rc::Rc};
use utilities::TimeStamp;

pub struct Transport {
    last_receive_state: CompleteTerminal,
    /* the underlying, encrypted network connection */
    connection: Rc<RefCell<Connection>>,
    /* sender side */
    sender: TransportSender,

    fragments: FragmentAssembly,
    receive_states: Vec<Rc<RefCell<TimestampState<CompleteTerminal>>>>,
}

impl Transport {
    pub fn new(
        initial_state: UserStream,
        initial_remote: CompleteTerminal,
        ip: &str,
        port: &str,
        key: &str,
    ) -> Self {
        let connection = Rc::new(RefCell::new(Connection::new(ip, port, key)));
        let mut receive_states = vec![];
        receive_states.push(Rc::new(RefCell::new(TimestampState::new(
            TimeStamp::timestamp(),
            0,
            initial_remote.clone(),
        ))));
        Transport {
            last_receive_state: initial_remote,
            connection: connection.clone(),
            sender: TransportSender::new(initial_state, connection),
            fragments: FragmentAssembly::new(),
            receive_states,
        }
    }

    pub fn tick(&mut self) {
        self.sender.tick();
    }

    pub fn push_back_event(&mut self, event: UserEvent) {
        self.sender.push_back_event(event);
    }

    pub fn recv(&self) -> Option<Vec<u8>> {
        self.connection.borrow().recv()
    }

    pub fn receive_packet(&mut self, bytes: Vec<u8>) {
        let bytes = self.connection.borrow_mut().recv_one(bytes);
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

                self.sender.process_acknowledgment_through(inst.ack_num());

                /* 1. make sure we don't already have the new state */
                for state in self.receive_states.iter() {
                    if inst.new_num() == state.borrow().num {
                        return;
                    }
                }
                /* 2. make sure we do have the old state */
                let mut reference_state: Option<Rc<RefCell<TimestampState<CompleteTerminal>>>> =
                    None;
                let mut found = false;
                for state in self.receive_states.iter() {
                    if inst.old_num() == state.borrow().num {
                        reference_state = Some(state.clone());
                        found = true;
                        break;
                    }
                }
                if !found {
                    return;
                }

                self.process_throwaway_until(inst.throwaway_num());

                let mut new_state = TimestampState::new(
                    TimeStamp::timestamp(),
                    inst.new_num(),
                    reference_state.as_ref().unwrap().borrow().state.clone(),
                );

                let mut data_acked = false;
                if !inst.diff().is_empty() {
                    new_state.state.apply_string(inst.diff(), inst.ack_num());
                    data_acked = true;
                }

                for i in 0..self.receive_states.len() {
                    let state = self.receive_states.get(i);
                    if let Some(state) = state.as_deref() {
                        if state.borrow().num > new_state.num {
                            warn!(
                                "Received OUT-OF-ORDER state {} [ack {}]",
                                new_state.num,
                                inst.ack_num()
                            );
                            self.receive_states
                                .insert(i, Rc::new(RefCell::new(new_state)));
                            return;
                        }
                    }
                }

                self.sender.set_ack_num(new_state.num);
                self.sender.remote_heard(new_state.timestamp);
                self.receive_states.push(Rc::new(RefCell::new(new_state)));

                if data_acked {
                    self.sender.set_data_ack();
                }
            }
        }
    }

    fn process_throwaway_until(&mut self, throwaway_num: u64) {
        // when sender's throwaway num equals receiver's ackNum there were problems
        self.receive_states
            .retain(|state| state.borrow().num >= throwaway_num);
        assert!(self.receive_states.len() > 0);
    }
}
