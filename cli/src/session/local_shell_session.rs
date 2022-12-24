use libs::TimeStamp;

use crate::{Session, ProtocolType};

pub struct LocalShellSession {
    id: u64,
    establish_time: u64
}

impl Session for LocalShellSession {
    type TYPE = super::LocalShellSession;

    fn create() -> Self::TYPE {
        LocalShellSession {
            id: LocalShellSession::gen_id(),
            establish_time: TimeStamp::timestamp(),
        }
    }

    fn id(&self) -> u64 {
        self.id
    }

    fn protocol(&self) -> crate::ProtocolType {
        ProtocolType::LocalShell
    }

    fn establish_time(&self) -> u64 {
        self.establish_time
    }
}