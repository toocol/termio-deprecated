mod mosh_session;
mod ssh_session;
mod session;

pub use mosh_session::*;
pub use ssh_session::*;
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
