#![allow(dead_code)]

use super::DEFAULT_SEND_MTU;

static BUFFER: [u8; DEFAULT_SEND_MTU] = [0u8; DEFAULT_SEND_MTU];
#[derive(Debug)]
pub struct Fragment {
    id: i64,
    finalize: bool,
    initialized: bool,
    contents: Vec<u8>,
    fragment_num: i16,
}
impl Default for Fragment {
    fn default() -> Self {
        Self {
            id: -1,
            finalize: false,
            initialized: false,
            contents: vec![],
            fragment_num: -1,
        }
    }
}

impl Fragment {
    pub fn from_id(id: i64, fragment_num: i16, finalize: bool, contents: Vec<u8>) -> Self {
        Fragment {
            id,
            finalize,
            initialized: true,
            contents,
            fragment_num,
        }
    }

    pub fn from_bytes(_bytes: &[u8]) -> Self {
        todo!()
    }
}
