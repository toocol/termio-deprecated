#![allow(dead_code)]
pub const KEY_LEN: usize = 16;

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

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_base_64_key() {
        let _key = Base64Key::new("zr0jtuYVKJnfJHP/XOOsbQ".to_string());
    }
}