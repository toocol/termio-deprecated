use super::{MAX_RTO, MIN_RTO, RTTVAR, SRIT};

pub struct Connection;
impl Connection {
    pub fn timeout(&self) -> u64 {
        let mut rto = (SRIT + 4. * RTTVAR).ceil() as u64;
        if rto < MIN_RTO {
            rto = MIN_RTO
        } else if rto > MAX_RTO {
            rto = MAX_RTO
        }
        rto
    }
}
