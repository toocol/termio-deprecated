use libs::TimeStamp;

use crate::{ProtocolType, Session};

pub struct RshSession {
    id: u64,
    establish_time: u64,
}

impl Session for RshSession {
    type TYPE = super::RshSession;

    fn create() -> Self::TYPE {
        RshSession {
            id: RshSession::gen_id(),
            establish_time: TimeStamp::timestamp(),
        }
    }

    fn id(&self) -> u64 {
        self.id
    }

    fn protocol(&self) -> crate::ProtocolType {
        ProtocolType::Rsh
    }

    fn establish_time(&self) -> u64 {
        self.establish_time
    }
}
