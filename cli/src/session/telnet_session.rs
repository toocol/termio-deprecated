use libs::TimeStamp;

use crate::{ProtocolType, Session};

pub struct TelnetSession {
    id: u64,
    establish_time: u64,
}

impl Session for TelnetSession {
    type TYPE = super::TelnetSession;

    fn create() -> Self::TYPE {
        TelnetSession {
            id: TelnetSession::gen_id(),
            establish_time: TimeStamp::timestamp(),
        }
    }

    fn id(&self) -> u64 {
        self.id
    }

    fn protocol(&self) -> crate::ProtocolType {
        ProtocolType::Telnet
    }

    fn establish_time(&self) -> u64 {
        self.establish_time
    }
}
