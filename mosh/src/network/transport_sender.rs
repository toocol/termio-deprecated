#![allow(dead_code)]
use std::{cell::RefCell, rc::Rc};

use libs::TimeStamp;

use crate::{
    crypto::{Prng, Session},
    network::{ACK_DELAY, ACK_INTERVAL},
    proto::transportinstruction::Instruction,
    statesync::{UserEvent, UserStream},
};

use super::{
    Connection, Fragmenter, MoshPacket, TimestampState, ACTIVE_RETRY_TIMEOUT, DEFAULT_SEND_MTU,
    MOSH_PROTOCOL_VERSION, SEND_INTERVAL_MAX, SEND_INTERVAL_MIN, SRIT,
};

pub struct TransportSender {
    current_state: UserStream,
    sent_states: Vec<Rc<RefCell<TimestampState<UserStream>>>>,
    fragmenter: Fragmenter,
    connection: Rc<RefCell<Connection>>,

    assumed_receiver_state: Rc<RefCell<TimestampState<UserStream>>>,

    next_ack_time: u64,
    next_send_time: i64,

    shutdown_tries: i32,
    shutdown_start: i64,

    /* information about receiver state */
    ack_num: u64,
    pending_data_ack: bool,

    /* ms to collect all input */
    send_min_delay: i32,
    last_heard: u64,

    min_delay_clock: i64,
}

impl TransportSender {
    pub fn new(initial_state: UserStream, connection: Rc<RefCell<Connection>>) -> Self {
        let sent_states = vec![];
        let timed_state = Rc::new(RefCell::new(TimestampState {
            timestamp: TimeStamp::timestamp(),
            num: 0,
            state: initial_state.clone(),
        }));
        let mut sender = TransportSender {
            current_state: initial_state,
            sent_states,
            fragmenter: Fragmenter::new(),
            connection,
            assumed_receiver_state: timed_state.clone(),
            next_ack_time: TimeStamp::timestamp(),
            next_send_time: TimeStamp::timestamp() as i64,
            shutdown_tries: 0,
            shutdown_start: -1,
            ack_num: 0,
            pending_data_ack: false,
            send_min_delay: 8,
            last_heard: 0,
            min_delay_clock: -1,
        };
        sender.sent_states.push(timed_state);
        sender
    }

    pub fn push_back_event(&mut self, user_event: UserEvent) {
        self.current_state.push_back(user_event)
    }

    pub fn tick(&mut self) {
        self.calculate_timers();

        let now = TimeStamp::timestamp();
        if now < self.next_ack_time && (now as i64) < self.next_send_time {
            return;
        }

        let diff = self
            .current_state
            .diff_from(&self.assumed_receiver_state.borrow().state);

        if diff.len() == 0 {
            if now >= self.next_ack_time {
                self.send_empty_ack();
                self.min_delay_clock = -1;
            }
            if (now as i64) >= self.next_send_time {
                self.next_send_time = -1;
                self.min_delay_clock = -1;
            }
        } else {
            self.send_to_receiver(diff);
            self.min_delay_clock = -1;
        }
    }

    pub fn process_acknowledgment_through(&mut self, ack_num: u64) {
        let mut iterator = self.sent_states.iter();
        let mut i: Rc<RefCell<TimestampState<UserStream>>>;
        let mut find = false;
        while let Some(next) = iterator.next() {
            i = next.clone();
            if i.borrow().num == ack_num {
                find = true;
                break;
            }
        }

        if find {
            self.sent_states.retain(|next| {
                if next.borrow().num < ack_num {
                    false
                } else {
                    true
                }
            })
        }
    }

    fn send_empty_ack(&mut self) {
        let now = TimeStamp::timestamp();
        assert!(now >= self.next_ack_time);

        let new_num = self.sent_states.last().unwrap().borrow().num + 1;

        self.add_sent_states(now, new_num, self.current_state.clone());
        self.send_in_fragments(vec![0u8; 0], new_num);

        self.next_ack_time = now + ACK_INTERVAL;
        self.next_send_time = -1;
    }

    fn send_to_receiver(&mut self, diff: Vec<u8>) {
        self.min_delay_clock = -1;
        let new_num;
        let back = self.sent_states.last().unwrap();
        if self.current_state == back.borrow().state {
            new_num = back.borrow().num;
        } else {
            new_num = back.borrow().num + 1;
        }

        if new_num == back.borrow().num {
            back.borrow_mut().timestamp = TimeStamp::timestamp();
        } else {
            self.add_sent_states(TimeStamp::timestamp(), new_num, self.current_state.clone());
        }

        self.send_in_fragments(diff, new_num);

        self.assumed_receiver_state = self.sent_states.last().unwrap().clone();
        self.next_ack_time = TimeStamp::timestamp() + ACK_INTERVAL;
        self.next_send_time = -1;
    }

    fn add_sent_states(&mut self, timestamp: u64, num: u64, state: UserStream) {
        self.sent_states
            .push(Rc::new(RefCell::new(TimestampState::new(
                timestamp, num, state,
            ))));
        if self.sent_states.len() > 32 {
            for _ in 0..15 {
                self.sent_states.remove(self.sent_states.len() - 1);
            }
        }
    }

    fn send_in_fragments(&mut self, diff: Vec<u8>, new_num: u64) {
        let mut inst = Instruction::new();
        inst.set_protocol_version(MOSH_PROTOCOL_VERSION);
        inst.set_old_num(self.assumed_receiver_state.borrow().num);
        inst.set_new_num(new_num);
        inst.set_ack_num(self.ack_num);
        inst.set_throwaway_num(self.sent_states.first().unwrap().borrow().num);
        inst.set_diff(diff);
        inst.set_chaff(self.make_chaff());

        let fragments = self.fragmenter.make_fragments(
            inst,
            DEFAULT_SEND_MTU - MoshPacket::ADDED_BYTES - Session::ADDED_BYTES,
        );
        for fragment in fragments {
            self.connection.borrow_mut().send(fragment.to_bytes());
        }

        self.pending_data_ack = false;
    }

    fn calculate_timers(&mut self) {
        let now = TimeStamp::timestamp();

        self.update_assumed_receiver_state();
        self.rationalize_states();

        if self.pending_data_ack && (self.next_ack_time > now + ACK_DELAY as u64) {
            self.next_ack_time = now + ACK_DELAY as u64;
        }

        if self.current_state != self.sent_states[self.sent_states.len() - 1].borrow().state {
            if self.min_delay_clock == -1 {
                self.min_delay_clock = now as i64;
            }
            self.next_send_time = (self.min_delay_clock + self.send_min_delay as i64).max(
                (self.sent_states.last().unwrap().borrow().timestamp + self.send_interval()) as i64,
            );
        } else if self.current_state != self.assumed_receiver_state.borrow().state
            && self.last_heard + ACTIVE_RETRY_TIMEOUT as u64 > now
        {
            self.next_send_time =
                (self.sent_states.last().unwrap().borrow().timestamp + self.send_interval()) as i64;
            if self.min_delay_clock == -1 {
                self.next_send_time = self
                    .next_send_time
                    .max(self.min_delay_clock + self.min_delay_clock);
            }
        } else if self.current_state != self.sent_states[0].borrow().state
            && self.last_heard + ACTIVE_RETRY_TIMEOUT as u64 > now
        {
            self.next_send_time = (self.sent_states.last().unwrap().borrow().timestamp
                + self.connection.borrow().timeout()
                + ACK_DELAY) as i64;
        } else {
            self.next_send_time = -1;
        }
    }

    fn update_assumed_receiver_state(&mut self) {
        let now = TimeStamp::timestamp();

        self.assumed_receiver_state = self.sent_states[0].clone();

        for i in 1..self.sent_states.len() {
            let state = &self.sent_states[i];
            assert!(now >= state.borrow().timestamp);
            if now - state.borrow().timestamp
                < self.connection.borrow().timeout() + ACK_DELAY as u64
            {
                self.assumed_receiver_state = state.clone()
            } else {
                return;
            }
        }
    }

    fn rationalize_states(&mut self) {
        self.current_state
            .subtract(&self.sent_states[0].borrow().state);

        let mut iterator = self.sent_states.iter().rev();
        let mut idx = self.sent_states.len() as i32 - 1;
        while let Some(prev) = iterator.next() {
            if idx == 0 {
                prev.borrow_mut().state.clear();
            } else {
                prev.borrow_mut()
                    .state
                    .subtract(&self.sent_states[0].borrow().state);
            }
            idx -= 1;
        }
    }

    fn send_interval(&self) -> u64 {
        let mut send_interval = (SRIT / 2.).ceil() as u64;
        if send_interval < SEND_INTERVAL_MIN {
            send_interval = SEND_INTERVAL_MIN;
        } else if send_interval > SEND_INTERVAL_MAX {
            send_interval = SEND_INTERVAL_MAX;
        }
        send_interval
    }

    fn make_chaff(&self) -> Vec<u8> {
        let chaff_max = 16;
        let chaff_len = Prng::uint8() % (chaff_max + 1);

        let mut chaff = vec![0u8; chaff_len as usize];
        Prng::fill(&mut chaff, chaff_len as usize);
        chaff
    }

    pub fn remote_heard(&mut self, timestamp: u64) {
        self.last_heard = timestamp;
    }

    pub fn connection(&self) -> Rc<RefCell<Connection>> {
        self.connection.clone()
    }

    pub fn set_send_delay(&mut self, send_min_delay: i32) {
        self.send_min_delay = send_min_delay;
    }

    pub fn set_ack_num(&mut self, ack_num: u64) {
        self.ack_num = ack_num;
    }

    pub fn set_data_ack(&mut self) {
        self.pending_data_ack = true;
    }
}
