#![allow(dead_code)]
use std::sync::atomic::AtomicU32;
use utilities::ByteOrder;

use super::{AeCtx, AeOcb, AE_SUCCESS};
pub const KEY_LEN: usize = 16;
pub const NONCE_LEN: usize = 12;
pub const RECEIVE_MTU: usize = 2048;
pub const ADDED_BYTES: usize = 16;

///////////////// Crypto
static COUNTER: AtomicU32 = AtomicU32::new(0);
pub struct Crypto;
impl Crypto {
    pub fn unique() -> u32 {
        COUNTER.fetch_add(1, std::sync::atomic::Ordering::SeqCst)
    }
}

///////////////// Nonce
pub struct Nonce {
    bytes: [u8; NONCE_LEN],
}
impl Nonce {
    pub fn from_seq(direction_seq: u64) -> Self {
        let be64 = ByteOrder::htobe64(direction_seq);
        let mut nonce = Nonce {
            bytes: [0; NONCE_LEN],
        };

        nonce.bytes[4..NONCE_LEN].copy_from_slice(&be64);

        nonce
    }

    pub fn from_bytes(bytes: &[u8], len: usize) -> Self {
        if len != 8 {
            panic!("Nonce representation must be 8 octets long.")
        }
        let mut nonce = Nonce {
            bytes: [0; NONCE_LEN],
        };

        nonce.bytes[4..NONCE_LEN].copy_from_slice(bytes);

        nonce
    }

    pub fn val(&self) -> u64 {
        let mut long_bytes = [0u8; 8];
        long_bytes.copy_from_slice(&self.bytes[4..NONCE_LEN]);
        ByteOrder::be64toh(long_bytes)
    }

    pub fn data(&self) -> &[u8] {
        &self.bytes
    }

    pub fn cc_bytes(&self) -> [u8; 8] {
        let mut cc = [0u8; 8];
        cc.copy_from_slice(&self.bytes[4..NONCE_LEN]);
        cc
    }
}

///////////////// Message
pub struct Message {
    pub nonce: Nonce,
    pub text: &'static [u8],
}
impl Message {
    pub fn new(nonce: Nonce, text: &'static [u8]) -> Self {
        Message { nonce, text }
    }

    pub fn get_timestamp(&self) -> u16 {
        ByteOrder::be16toh([self.text[0], self.text[1]])
    }

    pub fn get_timestamp_reply(&self) -> u16 {
        ByteOrder::be16toh([self.text[2], self.text[3]])
    }
}

///////////////// Base64Key
pub struct Base64Key {
    key: [u8; KEY_LEN],
}
impl Base64Key {
    pub fn new(printable_key: String) -> Self {
        let mut printable_key = printable_key;
        if printable_key.len() != 22 {
            panic!("Key must be 22 letters long.")
        };

        printable_key.push_str("==");
        let key = base64::decode(printable_key.as_bytes()).expect("Decode Base64Key failed.");
        if key.len() != KEY_LEN {
            panic!("Key must represent 16 octets.")
        };

        let mut base_64_key = Base64Key { key: [0; KEY_LEN] };
        for i in 0..KEY_LEN {
            base_64_key.key[i] = key[i]
        }

        if !printable_key.eq(&base_64_key.printable_key()) {
            panic!("Base64 key was not encoded 128-bit key.")
        }

        base_64_key
    }

    pub fn key(&self) -> [u8; KEY_LEN] {
        self.key
    }

    pub fn printable_key(&self) -> String {
        let base64_string = base64::encode(&self.key);
        let base64 = base64_string.as_bytes();

        if base64[22] != '=' as u8 || base64[23] != '=' as u8 {
            panic!("Unexpected output from base64_encode: {}", base64_string)
        }
        base64_string
    }
}

///////////////// AlignedBuffer
pub struct AlignedBuffer<'a> {
    pub len: usize,
    pub data: &'a [u8],
}
impl<'a> AlignedBuffer<'a> {
    pub fn new(data: &'a [u8]) -> Self {
        AlignedBuffer {
            len: data.len(),
            data,
        }
    }
}

///////////////// Session
pub struct Session<'a> {
    ctx: AeCtx,
    blocks_encrypted: u64,

    plain_text_buffer: AlignedBuffer<'a>,
    cipher_text_buffer: AlignedBuffer<'a>,
    nonce_buffer: AlignedBuffer<'a>,
}
impl<'a> Session<'a> {
    pub fn new(key: Base64Key) -> Self {
        let mut session = Session {
            ctx: AeCtx::new(),
            blocks_encrypted: 0,
            plain_text_buffer: AlignedBuffer::new(&[0u8; RECEIVE_MTU]),
            cipher_text_buffer: AlignedBuffer::new(&[0u8; RECEIVE_MTU]),
            nonce_buffer: AlignedBuffer::new(&[0u8; NONCE_LEN]),
        };

        if AE_SUCCESS != AeOcb::ae_init(&mut session.ctx, key.key(), 16, 12, 16) {
            panic!("Could not initialize AES-OCB context.")
        }
        session
    }

    pub fn encrypt(&self) -> Vec<u8> {
        todo!()
    }

    pub fn decrypt(&self) -> Message {
        todo!()
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_base_64_key() {
        let _key = Base64Key::new("zr0jtuYVKJnfJHP/XOOsbQ".to_string());
    }
}
