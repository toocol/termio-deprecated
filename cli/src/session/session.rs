use parking_lot::{MappedMutexGuard, Mutex, MutexGuard};
use libs::SnowflakeGuidGenerator;

use std::collections::HashMap;

use crate::{LocalShellSession, MoshSession, ProtocolType, RshSession, SshSession, TelnetSession};
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
        ProtocolType::Ssh => MutexGuard::map(SESSION_MAP.lock(), move |map| {
            let ssh_session = SshSession::create();
            let id = ssh_session.id();
            map.entry(id).or_insert(Box::new(Some(ssh_session)))
        })
        .id(),
        ProtocolType::Mosh => MutexGuard::map(SESSION_MAP.lock(), move |map| {
            let mosh_session = MoshSession::create();
            let id = mosh_session.id();
            map.entry(id).or_insert(Box::new(Some(mosh_session)))
        })
        .id(),
        ProtocolType::Telnet => MutexGuard::map(SESSION_MAP.lock(), move |map| {
            let telnet_session = TelnetSession::create();
            let id = telnet_session.id();
            map.entry(id).or_insert(Box::new(Some(telnet_session)))
        })
        .id(),
        ProtocolType::Rsh => MutexGuard::map(SESSION_MAP.lock(), move |map| {
            let rsh_session = RshSession::create();
            let id = rsh_session.id();
            map.entry(id).or_insert(Box::new(Some(rsh_session)))
        })
        .id(),
        ProtocolType::LocalShell => MutexGuard::map(SESSION_MAP.lock(), move |map| {
            let local_shell_session = LocalShellSession::create();
            let id = local_shell_session.id();
            map.entry(id).or_insert(Box::new(Some(local_shell_session)))
        })
        .id(),
    }
}

pub fn get_session(id: u64) -> MappedMutexGuard<'static, Box<dyn SessionWrapper>> {
    MutexGuard::map(SESSION_MAP.lock(), move |d| d.get_mut(&id).unwrap())
}

lazy_static! {
    pub static ref SESSION_MAP: Mutex<HashMap<u64, Box<dyn SessionWrapper>>> = {
        info!("Create session_map success.");
        Mutex::new(HashMap::new())
    };
}
