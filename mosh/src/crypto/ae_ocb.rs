#![allow(dead_code)]
use utilities::ByteOrder;
use aes::cipher::block_padding::NoPadding;

pub const AE_SUCCESS: i32 = 0;
pub const AE_INVALID: i32 = -1;
pub const AE_NOT_SUPPORTED: i32 = -2;

const L_TABLE_SIZE: usize = 16;
const OCB_KEY_LEN: usize = 16;
const OCB_TAG_LEN: usize = 16;
const BPI: usize = 4;
const TZ_TABLE: [u32; 32] = [
    0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26,
    12, 18, 6, 11, 5, 10, 9,
];

pub struct AeCtx {
    l: [Block; L_TABLE_SIZE],
    ktop_str: [u64; 3],

    offset: Block,
    checksum: Block,
    lstar: Block,
    ldollor: Block,
    ad_check_sum: Block,
    ad_offset: Block,
    cached_top: Block,
    ad_blocks_processed: i32,
    blocks_processed: i32,
}

impl AeCtx {
    type BLOCK_PADDING = NoPadding;
}

pub struct Block {
    pub l: u64,
    pub r: u64,
}

impl Block {
    pub fn new(l: u64, r: u64) -> Self {
        Block { l, r }
    }

    pub fn zero_block() -> Self {
        Block { l: 0, r: 0 }
    }

    pub fn double_block(bl: Block) -> Self {
        let mut b = Block::zero_block();
        let t = bl.l >> 63;
        // FIXME: Maybe `>>>` ?
        b.l = (bl.l + bl.l) ^ (bl.r >> 63);
        b.r = (bl.r + bl.r) ^ (t & 135);
        b
    }

    pub fn xor_block(x: Block, y: Block) -> Self {
        let mut b = Block::zero_block();
        b.l = x.l ^ y.l;
        b.r = x.r ^ y.r;
        b
    }

    pub fn from_bytes(bytes: [u8; 16]) -> Self {
        let mut block = Block::zero_block();

        let bytes8 = [
            bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7],
        ];
        block.l = ByteOrder::to_long(bytes8);

        let bytes8 = [
            bytes[8], bytes[9], bytes[10], bytes[11], bytes[12], bytes[13], bytes[14], bytes[15],
        ];
        block.r = ByteOrder::to_long(bytes8);

        block
    }

    /// if native byte order is little-endian, swap the bytes array;
    pub fn swap_if_le(b: Block) -> Self {
        if ByteOrder::little_endian() {
            Block::new(
                ByteOrder::to_long(ByteOrder::bswap64(b.l)),
                ByteOrder::to_long(ByteOrder::bswap64(b.r)),
            )
        } else {
            b
        }
    }

    pub fn gen_offset(ktop_str: [u64; 3], bot: u32) -> Block {
        let mut rval = Block::zero_block();
        if bot != 0 {
            rval.l = (ktop_str[0] << bot) | (ktop_str[1] >> (64 - bot));
            rval.r = (ktop_str[1] << bot) | (ktop_str[2] >> (64 - bot));
        } else {
            rval.l = ktop_str[0];
            rval.r = ktop_str[1];
        }
        Block::swap_if_le(rval)
    }

    pub fn unequal_blocks(x: &Block, y: &Block) -> bool {
        (((x).l ^ (y).l) | ((x).r ^ (y).r)) != 0
    }

    pub fn get_bytes(&self) -> [u8; 16] {
        let l_bytes = ByteOrder::long_bytes(self.l);
        let r_bytes = ByteOrder::long_bytes(self.r);
        [
            l_bytes[0], l_bytes[1], l_bytes[2], l_bytes[3], l_bytes[4], l_bytes[5], l_bytes[6],
            l_bytes[7], r_bytes[0], r_bytes[1], r_bytes[2], r_bytes[3], r_bytes[4], r_bytes[5],
            r_bytes[6], r_bytes[7],
        ]
    }
}
