pub struct ByteOrder;

impl ByteOrder {
    /// Whether is the little endian.
    pub fn little_endian() -> bool {
        cfg!(target_endian = "little")
    }

    /// Transfer little-endian long to big-endian [u8].
    pub fn h_to_be_64(x: u64) -> [u8; 8] {
        [
            ((x >> 56) & 0xFF) as u8,
            ((x >> 48) & 0xFF) as u8,
            ((x >> 40) & 0xFF) as u8,
            ((x >> 32) & 0xFF) as u8,
            ((x >> 24) & 0xFF) as u8,
            ((x >> 16) & 0xFF) as u8,
            ((x >> 8) & 0xFF) as u8,
            (x & 0xFF) as u8,
        ]
    }

    /// Transfer big-endian bytes to u64.
    pub fn be_64_to_h(bytes: [u8; 8]) -> u64 {
        u64::from_be_bytes(bytes)
    }

    /// Transfer little-endian u16 to big-endian byte[].
    pub fn h_to_be_16(x: u16) -> [u8; 2] {
        [((x >> 8) & 0xFF) as u8, (x & 0xFF) as u8]
    }

    /// Transfer big-endian bytes to u16.
    pub fn be_16_to_h(bytes: [u8; 2]) -> u16 {
        u16::from_be_bytes(bytes)
    }

    pub fn bswap64(x: u64) -> [u8; 8] {
        let mut array = ByteOrder::long_bytes(x);
        array.reverse();
        array
    }

    pub fn long_bytes(x: u64) -> [u8; 8] {
        if ByteOrder::little_endian() {
            x.to_le_bytes()
        } else {
            x.to_be_bytes()
        }
    }

    pub fn to_long(bytes: [u8; 8]) -> u64 {
        if ByteOrder::little_endian() {
            u64::from_le_bytes(bytes)
        } else {
            u64::from_be_bytes(bytes)
        }
    }

    pub fn int_bytes(x: u32) -> [u8; 4] {
        if ByteOrder::little_endian() {
            x.to_le_bytes()
        } else {
            x.to_be_bytes()
        }
    }

    pub fn to_int(bytes: [u8; 4]) -> u32 {
        if ByteOrder::little_endian() {
            u32::from_le_bytes(bytes)
        } else {
            u32::from_be_bytes(bytes)
        }
    }
}
