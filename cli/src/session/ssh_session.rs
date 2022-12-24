use libs::TimeStamp;

use crate::{ProtocolType, Session};

pub struct SshSession {
    id: u64,
    establish_time: u64,
}

impl Session for SshSession {
    type TYPE = super::SshSession;

    fn create() -> Self::TYPE {
        SshSession {
            id: SshSession::gen_id(),
            establish_time: TimeStamp::timestamp(),
        }
    }

    fn id(&self) -> u64 {
        self.id
    }

    fn protocol(&self) -> crate::ProtocolType {
        ProtocolType::Ssh
    }

    fn establish_time(&self) -> u64 {
        self.establish_time
    }
}
