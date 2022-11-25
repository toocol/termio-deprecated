#![allow(dead_code)]
pub struct TimestampState<State: Eq + PartialEq> {
    pub timestamp: u64,
    pub num: u64,
    pub state: State,
}

impl<State: Eq + PartialEq> TimestampState<State> {
    pub fn new(timestamp: u64, num: u64, state: State) -> Self {
        TimestampState {
            timestamp,
            num,
            state,
        }
    }
}
