#![allow(dead_code)]
#[repr(u8)]
#[derive(Default)]
pub enum ProtocolType {
    #[default]
    None = 0,
    Ssh,
    Mosh,
    Telnet,
    Rsh,
    LocalShell
}