#![allow(dead_code)]
use aes::{cipher::block_padding::NoPadding, Aes128Dec, Aes128Enc};
use cipher::{generic_array::GenericArray, BlockDecryptMut, BlockEncryptMut, KeyInit};
use once_cell::sync::OnceCell;
use utilities::ByteOrder;

use super::KEY_LEN;

pub const AE_SUCCESS: i32 = 0;
pub const AE_INVALID: i32 = -1;
pub const AE_NOT_SUPPORTED: i32 = -2;

const L_TABLE_SIZE: usize = 16;
const OCB_KEY_LEN: usize = 16;
const OCB_TAG_LEN: usize = 16;
const OCB_BLOCK_LEN: usize = 16;
const BPI: usize = 4;
const TZ_TABLE: [u32; 32] = [
    0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26,
    12, 18, 6, 11, 5, 10, 9,
];

pub struct AeOcb;

impl AeOcb {
    pub fn ae_init(ctx: &mut AeCtx, key: [u8; KEY_LEN], key_len: usize, nonce_len: usize, _tag_len: usize) -> i32 {
        if key_len != OCB_KEY_LEN {
            return AE_NOT_SUPPORTED
        }
        if nonce_len != 12 {
            return AE_NOT_SUPPORTED
        }
        let mut tmp_blk: Block;

        ctx.set_cipher(key);
        ctx.cached_top = Block::zero_block();
        ctx.ad_blocks_processed = 0;

        ctx.lstar = Block::from_bytes(ctx.encrypt(ctx.cached_top.get_bytes()));

        tmp_blk = Block::swap_if_le(&ctx.lstar);
        tmp_blk = Block::double_block(&tmp_blk);
        ctx.ldollor = Block::swap_if_le(&tmp_blk);

        tmp_blk = Block::double_block(&tmp_blk);
        ctx.l[0] = Block::swap_if_le(&tmp_blk);
        for i in 1..L_TABLE_SIZE {
            tmp_blk = Block::double_block(&tmp_blk);
            ctx.l[i] = Block::swap_if_le(&tmp_blk);
        }

        AE_SUCCESS
    }
}

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

    // Aes128 was implemented `ECB` mode directyly.
    encrypt_cipher: OnceCell<Aes128Enc>,
    decrypt_cipher: OnceCell<Aes128Dec>,
}

impl AeCtx {
    pub fn new() -> Self {
        AeCtx {
            l: [Block::zero_block(); L_TABLE_SIZE],
            ktop_str: [0; 3],
            offset: Block::zero_block(),
            checksum: Block::zero_block(),
            lstar: Block::zero_block(),
            ldollor: Block::zero_block(),
            ad_check_sum: Block::zero_block(),
            ad_offset: Block::zero_block(),
            cached_top: Block::zero_block(),
            ad_blocks_processed: 0,
            blocks_processed: 0,
            encrypt_cipher: OnceCell::new(),
            decrypt_cipher: OnceCell::new(),
        }
    }

    pub fn set_cipher(&self, key: [u8; OCB_KEY_LEN]) {
        let key = GenericArray::from(key);
        self.encrypt_cipher
            .set(Aes128Enc::new(&key))
            .expect("`encrypt_cipher` of AeCtx can only set once.");
        self.decrypt_cipher
            .set(Aes128Dec::new(&key))
            .expect("`encrypt_cipher` of AeCtx can only set once.");
    }

    pub fn encrypt(&self, mut data: [u8; OCB_BLOCK_LEN]) -> [u8; OCB_BLOCK_LEN] {
        let len = data.len();
        self.encrypt_cipher
            .get()
            .expect("`encrypt_cipher` of AeCtx is None.")
            .encrypt_padded_mut::<NoPadding>(&mut data, len)
            .expect("AeCtx encrypt failed");
        data
    }

    pub fn decrypt(&self, mut data: [u8; OCB_BLOCK_LEN]) -> [u8; OCB_BLOCK_LEN] {
        self.decrypt_cipher
            .get()
            .expect("`decrypt_cipher` of AeCtx is None.")
            .decrypt_padded_mut::<NoPadding>(&mut data)
            .expect("AeCtx encrypt failed");
        data
    }

    pub fn encrypt_block(&self, blocks: &mut [Block], bulks: usize) {
        for i in 0..bulks {
            blocks[i] = Block::from_bytes(self.encrypt(blocks[i].get_bytes()))
        }
    }

    pub fn decrypt_block(&self, blocks: &mut [Block], bulks: usize) {
        for i in 0..bulks {
            blocks[i] = Block::from_bytes(self.decrypt(blocks[i].get_bytes()))
        }
    }
}

#[derive(PartialEq, Eq, Debug)]
pub struct Block {
    pub l: u64,
    pub r: u64,
}

impl Clone for Block {
    fn clone(&self) -> Self {
        Self {
            l: self.l.clone(),
            r: self.r.clone(),
        }
    }
}

impl Copy for Block {}

impl Block {
    pub fn new(l: u64, r: u64) -> Self {
        Block { l, r }
    }

    pub fn zero_block() -> Self {
        Block { l: 0, r: 0 }
    }

    pub fn double_block(bl: &Block) -> Self {
        let mut b = Block::zero_block();
        let t = bl.l >> 63;
        // FIXME: Maybe `>>>` ?
        b.l = (bl.l + bl.l) ^ (bl.r >> 63);
        b.r = (bl.r + bl.r) ^ (t & 135);
        b
    }

    pub fn xor_block(x: &Block, y: &Block) -> Self {
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
    pub fn swap_if_le(b: &Block) -> Self {
        if ByteOrder::little_endian() {
            Block::new(
                ByteOrder::to_long(ByteOrder::bswap64(b.l)),
                ByteOrder::to_long(ByteOrder::bswap64(b.r)),
            )
        } else {
            b.clone()
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
        Block::swap_if_le(&rval)
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

#[cfg(test)]
mod test {
    use crate::crypto::Base64Key;

    use super::*;

    #[test]
    fn test_endian() {
        let mut block = Block::zero_block();
        block.l = 123123;
        let bs = ByteOrder::long_bytes(block.l);
        if ByteOrder::little_endian() {
            assert_eq!(bs, [243, 224, 1, 0, 0, 0, 0, 0,]);
        } else {
            assert_eq!(bs, [0, 0, 0, 0, 0, 1, 224, 243]);
        }

        block = Block::swap_if_le(&block);
        let bs = ByteOrder::long_bytes(block.l);
        assert_eq!(bs, [0, 0, 0, 0, 0, 1, 224, 243]);
    }

    #[test]
    fn test_ae_ctx() {
        let key = Base64Key::new("zr0jtuYVKJnfJHP/XOOsbQ".to_string());
        let ctx = AeCtx::new();
        ctx.set_cipher(key.key());

        let block = Block::new(123321, 123321);
        let bs = ctx.encrypt(block.get_bytes());
        assert_eq!(
            bs,
            [52, 101, 210, 59, 141, 136, 167, 79, 19, 190, 102, 81, 68, 52, 161, 232]
        );

        let bs = ctx.decrypt(bs);
        assert_eq!(bs, block.get_bytes());

        let block_de = Block::from_bytes(bs);
        assert_eq!(block_de, block);

        assert!(!Block::unequal_blocks(&block, &block_de));
    }
}
