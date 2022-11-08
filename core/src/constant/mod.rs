use serde::{Deserialize, Serialize};

pub const PROTOCOL_TYPE_SSH: i32 = 1;
pub const PROTOCOL_TYPE_MOSH: i32 = 2;

#[repr(i32)]
#[derive(Serialize, Deserialize, Debug, Clone, PartialEq, Eq, Hash)]
pub enum ProtocolType {
    Ssh = 1,
    Mosh,
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
        }
    }
}
