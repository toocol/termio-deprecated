#![allow(dead_code)]
use std::{cell::RefCell, rc::Rc};

use utilities::TimeStamp;

use crate::{
    network::ACK_DELAY,
    statesync::{UserEvent, UserStream},
};

use super::{
    Connection, Fragmenter, TimestampState, ACTIVE_RETRY_TIMEOUT, SEND_INTERVAL_MAX,
    SEND_INTERVAL_MIN, SRIT,
};

pub struct TransportSender {
    current_state: UserStream,
    sent_states: Vec<Rc<RefCell<TimestampState<UserStream>>>>,
    fragmenter: Fragmenter,
    connection: Connection,

    assumed_receiver_state: Rc<RefCell<TimestampState<UserStream>>>,

    next_ack_time: u64,
    next_send_time: i64,

    shutdown_tries: i32,
    shutdown_start: i64,

    /* information about receiver state */
    ack_num: i64,
    pending_data_ack: bool,

    /* ms to collect all input */
    send_min_delay: i32,
    last_heard: u64,

    min_delay_clock: i64,
}

impl TransportSender {
    pub fn new(initial_state: UserStream, connection: Connection) -> Self {
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

    pub fn tick(&self) {}

    fn calculate_timers(&mut self) {
        let now = TimeStamp::timestamp();

        self.update_assumed_receiver_state();
        self.rationalize_states();

        if self.pending_data_ack && (self.next_ack_time > now + ACK_DELAY as u64) {
            self.next_ack_time = now + ACK_DELAY as u64;
        }

        if self.current_state != self.sent_states[self.sent_states.len() - 1].borrow().state {
        } else if self.current_state != self.assumed_receiver_state.borrow().state
            && self.last_heard + ACTIVE_RETRY_TIMEOUT as u64 > now
        {
        } else if self.current_state != self.sent_states[0].borrow().state
            && self.last_heard + ACTIVE_RETRY_TIMEOUT as u64 > now
        {
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
            if now - state.borrow().timestamp < self.connection.timeout() + ACK_DELAY as u64 {
                self.assumed_receiver_state = state.clone()
            } else {
                return;
            }
        }
    }

    fn rationalize_states(&mut self) {
        let known_receiver_state = &self.sent_states[0].borrow().state;
        self.current_state.subtract(known_receiver_state);

        let mut iterator = self.sent_states.iter().rev();
        while let Some(prev) = iterator.next() {
            prev.borrow_mut().state.subtract(known_receiver_state);
        }
    }

    fn send_interval(&self) -> u32 {
        let mut send_interval = (SRIT / 2.).ceil() as u32;
        if send_interval < SEND_INTERVAL_MIN {
            send_interval = SEND_INTERVAL_MIN;
        } else if send_interval > SEND_INTERVAL_MAX {
            send_interval = SEND_INTERVAL_MAX;
        }
        send_interval
    }
}
