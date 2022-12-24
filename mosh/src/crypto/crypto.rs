#![allow(dead_code)]
use std::sync::atomic::AtomicU64;
use libs::ByteOrder;

use super::{AeCtx, AeOcb, AE_SUCCESS};
pub const KEY_LEN: usize = 16;
pub const NONCE_LEN: usize = 12;
pub const RECEIVE_MTU: usize = 2048;

///////////////// Crypto
static COUNTER: AtomicU64 = AtomicU64::new(0);
pub struct Crypto;
impl Crypto {
    pub fn unique() -> u64 {
        COUNTER.fetch_add(1, std::sync::atomic::Ordering::SeqCst)
    }
}

///////////////// Nonce
#[derive(Debug, PartialEq, Eq, Clone, Copy)]
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

        nonce.bytes[4..NONCE_LEN].copy_from_slice(&bytes[0..8]);

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
#[derive(Debug, PartialEq, Eq, Clone)]
pub struct Message {
    pub nonce: Nonce,
    pub text: Vec<u8>,
}
impl Message {
    pub fn new(nonce: Nonce, text: Vec<u8>) -> Self {
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
pub struct AlignedBuffer<const D: usize> {
    pub len: usize,
    pub data: [u8; D],
}
impl<const D: usize> AlignedBuffer<D> {
    pub fn new(data: [u8; D]) -> Self {
        AlignedBuffer {
            len: data.len(),
            data,
        }
    }

    pub fn len(&self) -> usize {
        self.len
    }
}

///////////////// Session
pub struct Session {
    ctx: AeCtx,
    blocks_encrypted: u64,

    plain_text_buffer: AlignedBuffer<RECEIVE_MTU>,
    cipher_text_buffer: AlignedBuffer<RECEIVE_MTU>,
    nonce_buffer: AlignedBuffer<NONCE_LEN>,
}
impl Session {
    pub const ADDED_BYTES: usize = 16;

    pub fn new(key: Base64Key) -> Self {
        let mut session = Session {
            ctx: AeCtx::new(),
            blocks_encrypted: 0,
            plain_text_buffer: AlignedBuffer::new([0u8; RECEIVE_MTU]),
            cipher_text_buffer: AlignedBuffer::new([0u8; RECEIVE_MTU]),
            nonce_buffer: AlignedBuffer::new([0u8; NONCE_LEN]),
        };

        if AE_SUCCESS != AeOcb::ae_init(&mut session.ctx, key.key(), 16, 12, 16) {
            panic!("Could not initialize AES-OCB context.")
        }
        session
    }

    pub fn encrypt(&mut self, plain_text: Message) -> Vec<u8> {
        let pt_len = plain_text.text.len();
        let cipher_text_len = pt_len + 16;

        assert!(cipher_text_len * 2 <= self.cipher_text_buffer.len());
        assert!(pt_len * 2 <= self.plain_text_buffer.len());

        self.plain_text_buffer.data[0..plain_text.text.len()]
            .copy_from_slice(&plain_text.text[0..plain_text.text.len()]);
        self.nonce_buffer.data[0..NONCE_LEN].copy_from_slice(plain_text.nonce.data());

        if cipher_text_len
            != AeOcb::ae_encrypt(
                &mut self.ctx,
                &mut self.nonce_buffer.data,
                &mut self.plain_text_buffer.data,
                pt_len,
                None,
                0usize,
                &mut self.cipher_text_buffer.data,
                None,
                1,
            ) as usize
        {
            panic!("aeEncrypt() returned error.")
        }

        self.blocks_encrypted += pt_len as u64 >> 4;
        if (pt_len & 0xF) > 0 {
            self.blocks_encrypted += 1;
        }

        if (self.blocks_encrypted >> 47) > 0 {
            panic!("Encrypted 2^47 blocks.")
        }

        let mut bytes: Vec<u8> = vec![0; cipher_text_len + 8];
        let cc_bytes = plain_text.nonce.cc_bytes();
        bytes[0..8].copy_from_slice(&cc_bytes[0..8]);
        bytes[8..8 + cipher_text_len]
            .copy_from_slice(&self.cipher_text_buffer.data[0..cipher_text_len]);

        bytes
    }

    pub fn decrypt(&mut self, str: &[u8], len: usize) -> Message {
        if len < 24 {
            panic!("Ciphertext must contain nonce and tag.")
        }

        let body_len = len - 8;
        if body_len < 16 {
            panic!("Mosh error, invalid message length.")
        }
        let pt_len = body_len - 16;

        assert!(body_len <= self.cipher_text_buffer.len());
        assert!(pt_len <= self.plain_text_buffer.len());

        let nonce = Nonce::from_bytes(str, 8);
        self.cipher_text_buffer.data[0..body_len].copy_from_slice(&str[8..8 + body_len]);
        self.nonce_buffer.data[0..NONCE_LEN].copy_from_slice(&nonce.data()[0..NONCE_LEN]);

        if pt_len
            != AeOcb::ae_decrypt(
                &mut self.ctx,
                &self.nonce_buffer.data,
                &self.cipher_text_buffer.data,
                body_len as i32,
                None,
                0usize,
                &mut self.plain_text_buffer.data,
                None,
                1,
            ) as usize
        {
            panic!("Packet failed integrity check.")
        }

        let mut text: Vec<u8> = vec![0; pt_len];
        text[0..pt_len].copy_from_slice(&self.plain_text_buffer.data[0..pt_len]);
        Message::new(nonce, text)
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_base_64_key() {
        let _key = Base64Key::new("zr0jtuYVKJnfJHP/XOOsbQ".to_string());
    }

    #[test]
    fn test_encrypt_decrypt() {
        let key = Base64Key::new("zr0jtuYVKJnfJHP/XOOsbQ".to_string());
        let mut session = Session::new(key);

        let plain_text = "Rust is a multi-paradigm, general-purpose programming language. Rust emphasizes performance, type safety, and concurrency.[11][12][13] 
            Rust enforces memory safety—that is, that all references point to valid memory—without requiring the use of a garbage collector or reference counting present in other memory-safe languages.[13][14]";
        let nonce = Nonce::from_seq(10);

        let bytes = plain_text.as_bytes();
        let mut text: Vec<u8> = vec![0; bytes.len()];
        text[0..bytes.len()].copy_from_slice(bytes);

        let en_message = Message::new(nonce, text);
        let encrypted = session.encrypt(en_message.clone());
        let de_message = session.decrypt(&encrypted, encrypted.len());
        assert_eq!(en_message, de_message);
    }
}
