use serde::{Deserialize, Serialize};

pub const PROTOCOL_TYPE_SSH: i32 = 1;
pub const PROTOCOL_TYPE_MOSH: i32 = 2;
pub const PROTOCOL_TYPE_TELNET: i32 = 3;
pub const PROTOCOL_TYPE_RSH: i32 = 4;

#[repr(i32)]
#[derive(Serialize, Deserialize, Debug, Clone, PartialEq, Eq, Hash)]
pub enum ProtocolType {
    Ssh = 1,
    Mosh,
    Telnet,
    Rsh,
}

impl ProtocolType {
    pub fn from_int(ctype: i32) -> ProtocolType {
        match ctype {
            PROTOCOL_TYPE_SSH => ProtocolType::Ssh,
            PROTOCOL_TYPE_MOSH => ProtocolType::Mosh,
            _ => panic!(
                "Protocol type only support 1=[Ssh], 2=[Mosh]. Get {}",
                ctype
            ),
        }
    }

    pub fn to_int(&self) -> i32 {
        match self {
            ProtocolType::Ssh => PROTOCOL_TYPE_SSH,
            ProtocolType::Mosh => PROTOCOL_TYPE_MOSH,
            ProtocolType::Telnet => PROTOCOL_TYPE_TELNET,
            ProtocolType::Rsh => PROTOCOL_TYPE_RSH,
        }
    }

    pub fn as_str(&self) -> &'static str {
        match self {
            ProtocolType::Ssh => "SSH",
            ProtocolType::Mosh => "Mosh",
            ProtocolType::Telnet => "Telnet",
            ProtocolType::Rsh => "Rsh",
        }
    }
}
