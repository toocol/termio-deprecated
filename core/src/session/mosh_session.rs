use utilities::TimeStamp;

use crate::{ProtocolType, Session};

pub struct MoshSession {
    id: u64,
    establish_time: u64,
}

impl Session for MoshSession {
    type TYPE = super::MoshSession;

    fn create() -> Self::TYPE {
        MoshSession {
            id: MoshSession::gen_id(),
            establish_time: TimeStamp::timestamp(),
        }
    }

    fn id(&self) -> u64 {
        self.id
    }

    fn protocol(&self) -> crate::ProtocolType {
        ProtocolType::Mosh
    }

    fn establish_time(&self) -> u64 {
        self.establish_time
    }
}
