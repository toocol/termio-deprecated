#![allow(dead_code)]
#[derive(Debug, PartialEq, Eq, Clone)]
pub struct TimestampState<State: Eq + PartialEq + Clone> {
    pub timestamp: u64,
    pub num: u64,
    pub state: State,
}

impl<State: Eq + PartialEq + Clone> TimestampState<State> {
    pub fn new(timestamp: u64, num: u64, state: State) -> Self {
        TimestampState {
            timestamp,
            num,
            state,
        }
    }
}
