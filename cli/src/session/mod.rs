mod local_shell_session;
mod mosh_session;
mod ssh_session;
mod telnet_session;
mod rsh_session;
mod session;

pub use local_shell_session::*;
pub use mosh_session::*;
pub use ssh_session::*;
pub use telnet_session::*;
pub use rsh_session::*;
pub use session::*;

#[cfg(test)]
mod tests {
    use crate::ProtocolType;
    use super::*;

    #[test]
    fn test_sessions() {
        let session_id = create_session(ProtocolType::Ssh);
        assert_ne!(0, session_id);
    }
}
