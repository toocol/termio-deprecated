use utilities::SnowflakeGuidGenerator;

use std::{collections::HashMap, sync::Mutex};

use crate::{ProtocolType, SshSession, MoshSession};
use lazy_static::lazy_static;
use log::info;

pub trait Session: Sync + Send {
    type TYPE;

    /// Create a new session.
    fn create() -> Self::TYPE;

    /// Generate global unique u64 id.
    fn gen_id() -> u64 {
        SnowflakeGuidGenerator::next_id().expect("`SnowflakeGuidGenerator` generate id failed.")
    }

    /// Get the global unique id.
    fn id(&self) -> u64;

    /// Get the connection protocol of the session.
    fn protocol(&self) -> ProtocolType;

    /// Get the timestamp of session establishment.
    fn establish_time(&self) -> u64;
}

pub trait SessionWrapper: Send + Sync {
    fn id(&self) -> u64;

    fn protocol(&self) -> ProtocolType;

    fn establish_time(&self) -> u64;
}

impl<T: Session> SessionWrapper for Option<T> {
    fn id(&self) -> u64 {
        self.as_ref()
            .expect("`Wrapper` session can not be None.")
            .id()
    }

    fn protocol(&self) -> ProtocolType {
        self.as_ref()
            .expect("`Wrapper` session can not be None.")
            .protocol()
    }

    fn establish_time(&self) -> u64 {
        self.as_ref()
            .expect("`Wrapper` session can not be None.")
            .establish_time()
    }
}

/// Create a new session base on protocol, and return it's id.
pub fn create_session(protocol: ProtocolType) -> u64 {
    match protocol {
        ProtocolType::Ssh => {
            if let Ok(mut map_guard) = SESSION_MAP.lock() {
                let ssh_session = SshSession::create();
                let id = ssh_session.id();
                let wrapper_map = map_guard.entry(protocol).or_insert(HashMap::new());
                wrapper_map.insert(id, Box::new(Some(ssh_session)));
                id
            } else {
                panic!("`SESSION_MAP` get lock error.")
            }
        }
        ProtocolType::Mosh => {
            if let Ok(mut map_guard) = SESSION_MAP.lock() {
                let mosh_session = MoshSession::create();
                let id = mosh_session.id();
                let wrapper_map = map_guard.entry(protocol).or_insert(HashMap::new());
                wrapper_map.insert(id, Box::new(Some(mosh_session)));
                id
            } else {
                panic!("`SESSION_MAP` get lock error.")
            }
        }
    }
}

lazy_static! {
    pub static ref SESSION_MAP: Mutex<HashMap<ProtocolType, HashMap<u64, Box<dyn SessionWrapper>>>> = {
        info!("Create session_map success.");
        Mutex::new(HashMap::new())
    };
}
