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
const TZ_TABLE: [usize; 32] = [
    0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26,
    12, 18, 6, 11, 5, 10, 9,
];

pub struct AeOcb;

impl AeOcb {
    pub fn ae_init(
        ctx: &mut AeCtx,
        key: [u8; KEY_LEN],
        key_len: usize,
        nonce_len: usize,
        _tag_len: usize,
    ) -> i32 {
        if key_len != OCB_KEY_LEN {
            return AE_NOT_SUPPORTED;
        }
        if nonce_len != 12 {
            return AE_NOT_SUPPORTED;
        }
        let mut tmp_blk: Block;

        ctx.set_cipher(key);
        ctx.cached_top = Block::zero_block();
        ctx.ad_checksum = Block::zero_block();
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

    pub fn ae_encrypt(
        ctx: &mut AeCtx,
        nonce: &[u8],
        pt: &[u8],
        mut pt_len: usize,
        _ad: Option<&[u8]>,
        _ad_len: usize,
        ct: &mut [u8],
        mut tag: Option<&mut [u8]>,
        finalize: i32,
    ) -> i32 {
        let ptp: Vec<Block>;
        let mut ctp: [Block; BPI];
        let mut offset: &Block;
        let mut checksum: Block;
        let mut tmp_bl = Block::zero_block();
        let mut tmp_u8: [u8; 16];

        let i: usize;
        let mut j: usize;
        let mut k: usize;

        if nonce.len() > 0 {
            ctx.offset = AeOcb::gen_offset_from_nonce(ctx, nonce);
            ctx.ad_offset = Block::zero_block();
            ctx.checksum = Block::zero_block();
            ctx.ad_blocks_processed = 0;
            ctx.blocks_processed = 0;
            ctx.ad_checksum = Block::zero_block();
        }

        offset = &ctx.offset;
        checksum = ctx.checksum.clone();
        i = pt_len / (BPI * 16);

        ptp = AeOcb::transfer_block_array(pt, i);
        ctp = [Block::zero_block(); BPI];

        j = 0;
        if i > 0 {
            let mut oa = [Block::zero_block(); BPI];
            let mut block_num = ctx.blocks_processed;
            oa[BPI - 1] = offset.clone();

            loop {
                let mut ta = [Block::zero_block(); BPI];
                block_num += BPI;

                oa[0] = Block::xor_block(&oa[BPI - 1], &ctx.l[0]);
                ta[0] = Block::xor_block(&oa[0], &ptp[j * BPI]);
                checksum = Block::xor_block(&checksum, &ptp[j * BPI]);

                oa[1] = Block::xor_block(&oa[0], &ctx.l[1]);
                ta[1] = Block::xor_block(&oa[1], &ptp[j * BPI + 1]);
                checksum = Block::xor_block(&checksum, &ptp[j * BPI + 1]);

                oa[2] = Block::xor_block(&oa[1], &ctx.l[0]);
                ta[2] = Block::xor_block(&oa[2], &ptp[j * BPI + 2]);
                checksum = Block::xor_block(&checksum, &ptp[j * BPI + 2]);

                oa[3] = Block::xor_block(&oa[2], &ctx.l[AeOcb::ntz(block_num as i32)]);
                ta[3] = Block::xor_block(&oa[3], &ptp[j * BPI + 3]);
                checksum = Block::xor_block(&checksum, &ptp[j * BPI + 3]);

                ctx.encrypt_block(&mut ta, BPI);

                ctp[0] = Block::xor_block(&ta[0], &oa[0]);
                ctp[1] = Block::xor_block(&ta[1], &oa[1]);
                ctp[2] = Block::xor_block(&ta[2], &oa[2]);
                ctp[3] = Block::xor_block(&ta[3], &oa[3]);
                AeOcb::fill_data_from_block_arrays(ct, &ctp, j, ctp.len());

                j += 1;
                if j >= i {
                    break;
                }
            }

            ctx.offset = oa[BPI - 1];
            offset = &ctx.offset;
            ctx.blocks_processed = block_num;
            ctx.checksum = checksum;
        }
        AeOcb::initial_blocks(&mut ctp);

        if finalize > 0 {
            let mut ta = [Block::zero_block(); BPI + 1];
            let mut oa = [Block::zero_block(); BPI];
            let mut remaining = pt_len % (BPI * 16);
            k = 0;

            if remaining > 0 {
                if remaining >= 32 {
                    oa[k] = Block::xor_block(offset, &ctx.l[0]);
                    ta[k] = Block::xor_block(&oa[k], &ptp[j * BPI + k]);
                    checksum = Block::xor_block(&checksum, &ptp[j * BPI + k]);
                    oa[k + 1] = Block::xor_block(&oa[k], &ctx.l[1]);
                    offset = &oa[k + 1];
                    ta[k + 1] = Block::xor_block(offset, &ptp[j * BPI + k + 1]);
                    checksum = Block::xor_block(&checksum, &ptp[j * BPI + k + 1]);
                    remaining -= 32;
                    k += 2;
                }
                if remaining >= 16 {
                    oa[k] = Block::xor_block(offset, &ctx.l[0]);
                    offset = &oa[k];
                    ta[k] = Block::xor_block(offset, &ptp[j * BPI + k]);
                    checksum = Block::xor_block(&checksum, &ptp[j * BPI + k]);
                    remaining -= 16;
                    k += 1;
                }
                if remaining > 0 {
                    //memcpy(tmp.u8, ptp+k, remaining);
                    tmp_u8 = [0u8; 16];
                    let src_pos = j * BPI * 16 + k * 16;
                    tmp_u8[0..remaining].copy_from_slice(&pt[src_pos..src_pos + remaining]);

                    tmp_u8[remaining] = 0x80;
                    tmp_bl = Block::from_bytes(tmp_u8);
                    checksum = Block::xor_block(&checksum, &tmp_bl);
                    ta[k] = Block::xor_block(offset, &ctx.lstar);
                    offset = &ta[k];
                    k += 1;
                }
            }

            let blk = Block::xor_block(offset, &ctx.ldollor);
            offset = &blk;
            ta[k] = Block::xor_block(offset, &checksum);
            ctx.encrypt_block(&mut ta, k + 1);
            let blk = Block::xor_block(&ta[k], &ctx.ad_checksum);
            offset = &blk;
            if remaining > 0 {
                k -= 1;
                tmp_bl = Block::xor_block(&tmp_bl, &ta[k]);
                tmp_u8 = tmp_bl.get_bytes();

                // memcpy(ctp+k, tmp.u8, remaining);
                let mut bytes = [0u8; 16];
                bytes[0..remaining].copy_from_slice(&tmp_u8[0..remaining]);
                ctp[k] = Block::from_bytes(bytes);
            }

            match k {
                3 => {
                    ctp[2] = Block::xor_block(&ta[2], &oa[2]);
                    ctp[1] = Block::xor_block(&ta[1], &oa[1]);
                    ctp[0] = Block::xor_block(&ta[0], &oa[0]);
                }
                2 => {
                    ctp[1] = Block::xor_block(&ta[1], &oa[1]);
                    ctp[0] = Block::xor_block(&ta[0], &oa[0]);
                }
                1 => ctp[0] = Block::xor_block(&ta[0], &oa[0]),
                _ => {}
            }
            AeOcb::fill_data_from_block_arrays(ct, &ctp, j, ctp.len());

            if let Some(tag) = tag.take() {
                tag[0..OCB_TAG_LEN].copy_from_slice(&offset.get_bytes()[0..OCB_TAG_LEN]);
            } else {
                ct[pt_len..pt_len + OCB_TAG_LEN]
                    .copy_from_slice(&offset.get_bytes()[0..OCB_TAG_LEN]);
                pt_len += OCB_TAG_LEN;
            }
        }

        pt_len as i32
    }

    pub fn ae_decrypt(
        ctx: &mut AeCtx,
        nonce: &[u8],
        ct: &[u8],
        mut ct_len: i32,
        _ad: Option<&[u8]>,
        _ad_len: usize,
        pt: &mut [u8],
        mut tag: Option<&[u8; 16]>,
        finalize: i32,
    ) -> i32 {
        let i: usize;
        let mut j: usize;
        let mut k: usize;

        let mut tmp_u8 = [0u8; 16];
        let mut tmp_bl: Block;

        let mut offset: &Block;
        let mut checksum: Block;
        let blk: Block;

        let ctp: Vec<Block>;
        let mut ptp: [Block; BPI];

        if finalize > 0 && tag.is_none() {
            ct_len -= OCB_TAG_LEN as i32;
        }

        if nonce.len() > 0 {
            ctx.offset = AeOcb::gen_offset_from_nonce(ctx, nonce);
            ctx.ad_offset = Block::zero_block();
            ctx.checksum = Block::zero_block();
            ctx.blocks_processed = 0;
            ctx.ad_blocks_processed = 0;
            ctx.ad_checksum = Block::zero_block();
        }

        offset = &ctx.offset;
        checksum = ctx.checksum.clone();
        i = ct_len as usize / (BPI * 16);
        j = 0;

        ctp = AeOcb::transfer_block_array(ct, i);
        ptp = [Block::zero_block(); BPI];

        if i > 0 {
            let mut oa = [Block::zero_block(); BPI];
            let mut block_num = ctx.blocks_processed;
            oa[BPI - 1] = offset.clone();
            loop {
                let mut ta = [Block::zero_block(); BPI];
                block_num += BPI;

                oa[0] = Block::xor_block(&oa[BPI - 1], &ctx.l[0]);
                ta[0] = Block::xor_block(&oa[0], &ctp[j * BPI]);

                oa[1] = Block::xor_block(&oa[0], &ctx.l[1]);
                ta[1] = Block::xor_block(&oa[1], &ctp[j * BPI + 1]);

                oa[2] = Block::xor_block(&oa[1], &ctx.l[0]);
                ta[2] = Block::xor_block(&oa[2], &ctp[j * BPI + 2]);

                oa[3] = Block::xor_block(&oa[2], &ctx.l[AeOcb::ntz(block_num as i32)]);
                ta[3] = Block::xor_block(&oa[3], &ctp[j * BPI + 3]);

                ctx.decrypt_block(&mut ta, BPI);

                ptp[0] = Block::xor_block(&ta[0], &oa[0]);
                checksum = Block::xor_block(&checksum, &ptp[0]);
                ptp[1] = Block::xor_block(&ta[1], &oa[1]);
                checksum = Block::xor_block(&checksum, &ptp[1]);
                ptp[2] = Block::xor_block(&ta[2], &oa[2]);
                checksum = Block::xor_block(&checksum, &ptp[2]);
                ptp[3] = Block::xor_block(&ta[3], &oa[3]);
                checksum = Block::xor_block(&checksum, &ptp[3]);
                AeOcb::fill_data_from_block_arrays(pt, &ptp, j, ptp.len());

                j += 1;
                if j >= i {
                    break;
                }
            }

            ctx.offset = oa[BPI - 1];
            offset = &ctx.offset;
            ctx.blocks_processed = block_num;
            ctx.checksum = checksum;
        }

        if finalize > 0 {
            let mut ta = [Block::zero_block(); BPI + 1];
            let mut oa = [Block::zero_block(); BPI];
            let mut remaining = ct_len as usize % (BPI * 16);
            k = 0;

            if remaining > 0 {
                if remaining >= 32 {
                    oa[k] = Block::xor_block(offset, &ctx.l[0]);
                    ta[k] = Block::xor_block(&oa[k], &ctp[j * BPI + k]);
                    oa[k + 1] = Block::xor_block(&oa[k], &ctx.l[1]);
                    offset = &oa[k + 1];
                    ta[k + 1] = Block::xor_block(offset, &ctp[j * BPI + k + 1]);
                    remaining -= 32;
                    k += 2;
                }
                if remaining >= 16 {
                    oa[k] = Block::xor_block(offset, &ctx.l[0]);
                    offset = &oa[k];
                    ta[k] = Block::xor_block(&offset, &ctp[j * BPI + k]);
                    remaining -= 16;
                    k += 1;
                }
                if remaining > 0 {
                    let pad: Block;
                    blk = Block::xor_block(offset, &ctx.lstar);
                    offset = &blk;
                    let encrypt = ctx.encrypt(offset.get_bytes());
                    tmp_u8[0..encrypt.len()].copy_from_slice(&encrypt[0..encrypt.len()]);
                    pad = Block::from_bytes(tmp_u8);

                    // memcpy(tmp.u8,ctp+k,remaining);
                    let src_pos = k * 16 + j * BPI * 16;
                    tmp_u8[0..remaining].copy_from_slice(&ct[src_pos..src_pos + remaining]);

                    tmp_bl = Block::from_bytes(tmp_u8);
                    tmp_bl = Block::xor_block(&tmp_bl, &pad);

                    tmp_u8 = tmp_bl.get_bytes();
                    tmp_u8[remaining] = 0x80;

                    // memcpy(ptp+k, tmp.u8, remaining);
                    let dest_pos = k * 16 + j * BPI * 16;
                    pt[dest_pos..dest_pos + remaining].copy_from_slice(&tmp_u8[0..remaining]);
                    let bytes = ptp[k].get_bytes();
                    tmp_u8[0..remaining].copy_from_slice(&bytes[0..remaining]);
                    ptp[k] = Block::from_bytes(bytes);

                    tmp_bl = Block::from_bytes(tmp_u8);
                    checksum = Block::xor_block(&checksum, &tmp_bl);
                }
            }

            ctx.decrypt_block(&mut ta, k);
            match k {
                3 => {
                    ptp[2] = Block::xor_block(&ta[2], &oa[2]);
                    checksum = Block::xor_block(&checksum, &ptp[2]);

                    ptp[1] = Block::xor_block(&ta[1], &oa[1]);
                    checksum = Block::xor_block(&checksum, &ptp[1]);

                    ptp[0] = Block::xor_block(&ta[0], &oa[0]);
                    checksum = Block::xor_block(&checksum, &ptp[0]);
                }
                2 => {
                    ptp[1] = Block::xor_block(&ta[1], &oa[1]);
                    checksum = Block::xor_block(&checksum, &ptp[1]);

                    ptp[0] = Block::xor_block(&ta[0], &oa[0]);
                    checksum = Block::xor_block(&checksum, &ptp[0]);
                }
                1 => {
                    ptp[0] = Block::xor_block(&ta[0], &oa[0]);
                    checksum = Block::xor_block(&checksum, &ptp[0]);
                }
                _ => {}
            }

            AeOcb::fill_data_from_block_arrays(pt, &ptp, j, ptp.len());

            let blk = Block::xor_block(offset, &ctx.ldollor);
            offset = &blk;
            tmp_bl = Block::xor_block(offset, &checksum);
            tmp_u8 = tmp_bl.get_bytes();
            tmp_u8 = ctx.encrypt(tmp_u8);
            tmp_bl = Block::from_bytes(tmp_u8);
            tmp_bl = Block::xor_block(&tmp_bl, &ctx.ad_checksum);
            tmp_u8 = tmp_bl.get_bytes();

            if OCB_TAG_LEN == 16 && tag.is_some() {
                if Block::unequal_blocks(&tmp_bl, &Block::from_bytes(tag.take().unwrap().clone())) {
                    ct_len = AE_INVALID;
                }
            } else {
                let len = OCB_TAG_LEN;

                let mut tmp = [0u8; OCB_TAG_LEN];
                tmp.copy_from_slice(&ct[ct_len as usize..ct_len as usize + len]);
                if AeOcb::constant_time_memcmp(&tmp, &tmp_u8, len) != 0 {
                    ct_len = AE_INVALID;
                }
            }
        }

        ct_len
    }

    fn ntz(x: i32) -> usize {
        TZ_TABLE[(((x & -x) * 0x077CB531) >> 27) as usize]
    }

    fn initial_blocks(blocks: &mut [Block]) {
        for i in 0..blocks.len() {
            blocks[i] = Block::zero_block();
        }
    }

    fn gen_offset_from_nonce(ctx: &mut AeCtx, nonce: &[u8]) -> Block {
        let idx: u8;
        let mut bytes16 = [0u8; 16];
        let mut tmp = [0u32; 4];
        tmp[0] = if ByteOrder::little_endian() {
            0x01000000
        } else {
            0x00000001
        };
        let mut bytes4 = [0u8; 4];
        for i in 1usize..4 {
            bytes4.copy_from_slice(&nonce[(i - 1) * 4..(i - 1) * 4 + 4]);
            tmp[i] = ByteOrder::to_int(bytes4.clone());
        }

        for i in 0..tmp.len() {
            let bytes = ByteOrder::int_bytes(tmp[i]);
            bytes16[i * 4..i * 4 + 4].copy_from_slice(&bytes);
        }

        // Get low 6 bits of nonce
        idx = bytes16[15] & 0x3f;
        // Zero low 6 bits of nonce
        bytes16[15] = bytes16[15] & 0xc0;

        let tmp_blk = Block::from_bytes(bytes16);

        if Block::unequal_blocks(&tmp_blk, &ctx.cached_top) {
            ctx.cached_top = tmp_blk;
            let encrypt = ctx.encrypt(tmp_blk.get_bytes());
            let mut ktop_blk = Block::from_bytes(encrypt);
            ktop_blk = Block::swap_if_le(&ktop_blk);

            ctx.ktop_str[0] = ktop_blk.l;
            ctx.ktop_str[1] = ktop_blk.r;
            ctx.ktop_str[2] = ctx.ktop_str[0] ^ (ctx.ktop_str[0] << 8) ^ (ctx.ktop_str[1] >> 56);
        }

        Block::gen_offset(&ctx.ktop_str, idx as u32)
    }

    fn transfer_block_array(bytes: &[u8], mut round: usize) -> Vec<Block> {
        round = round + 1;

        let mut blks: Vec<Block> = vec![];
        let gap = 16usize;

        for i in 0..BPI * round {
            let mut bytes16 = [0u8; 16];
            for j in 0..gap {
                let val = if i * gap + j >= bytes.len() {
                    0
                } else {
                    bytes[i * gap + j]
                };
                bytes16[j] = val;
            }
            blks.push(Block::from_bytes(bytes16));
        }
        blks
    }

    fn fill_data_from_block_arrays(
        target: &mut [u8],
        blocks: &[Block; 4],
        round: usize,
        end: usize,
    ) {
        for i in 0..end {
            let dest_pos = (round * BPI * 16) + (i * 16);
            target[dest_pos..dest_pos + 16].copy_from_slice(&blocks[i].get_bytes());
        }
    }

    fn constant_time_memcmp(av: &[u8], bv: &[u8], n: usize) -> u8 {
        let mut result = 0u8;

        for i in 0..n as usize {
            result |= av[i] ^ bv[i];
        }

        result
    }
}

pub struct AeCtx {
    l: [Block; L_TABLE_SIZE],
    ktop_str: [u64; 3],

    offset: Block,
    checksum: Block,
    lstar: Block,
    ldollor: Block,
    ad_checksum: Block,
    ad_offset: Block,
    cached_top: Block,
    ad_blocks_processed: usize,
    blocks_processed: usize,

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
            ad_checksum: Block::zero_block(),
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
        let t = (bl.l as i64 >> 63) as u64;
        // FIXME: Maybe `>>>` ?
        b.l = (bl.l as u128 + bl.l as u128) as u64 ^ (bl.r >> 63);
        b.r = (bl.r as u128 + bl.r as u128) as u64 ^ (t & 135);
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

    pub fn gen_offset(ktop_str: &[u64; 3], bot: u32) -> Block {
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
