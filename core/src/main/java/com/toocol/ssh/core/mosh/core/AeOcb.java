package com.toocol.ssh.core.mosh.core;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static com.toocol.ssh.core.mosh.core.AeOcb.Block.swapIfLe;
import static com.toocol.ssh.core.mosh.core.AeOcb.Block.zeroBlock;
import static com.toocol.ssh.core.mosh.core.ByteOrder.bswap64;

/**
 * ae.h/ocb.cc
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/2 22:28
 * @version: 0.0.1
 */
public class AeOcb {
    public static final int AE_SUCCESS = 0;
    public static final int AE_INVALID = -1;
    public static final int AE_NOT_SUPPORTED = -2;

    private static final int OCB_KEY_LEN = 16;

    public static class Block {
        long l;
        long r;

        Block(long l, long r) {
            this.l = l;
            this.r = r;
        }

        byte[] getBytes() {
            byte[] bytes = new byte[16];
            byte[] lBytes = Longs.toByteArray(l);
            byte[] rBytes = Longs.toByteArray(r);
            System.arraycopy(lBytes, 0, bytes, 0, 8);
            System.arraycopy(rBytes, 0, bytes, 8, 8);
            return bytes;
        }

        void fromBytes(byte[] bytes) {
            if (bytes.length != 16) {
                return;
            }

            byte[] bytes8 = new byte[8];
            System.arraycopy(bytes, 0, bytes8, 0, 8);
            this.l = Longs.fromByteArray(bytes8);
            System.arraycopy(bytes, 8, bytes8, 0, 8);
            this.r = Longs.fromByteArray(bytes8);
        }

        void doubleBlock() {
            Block b = this;
            long t = b.l >> 63;
            b.l = (b.l + b.l) ^ (b.r >> 63);
            b.r = (b.r + b.r) ^ (t & 135);
        }

        static Block xorBlock(Block x, Block y) {
            x.l ^= y.l;
            x.r ^= y.r;
            return x;
        }

        static Block zeroBlock() {
            return new Block(0, 0);
        }

        /**
         * if native byte order is little-endian, swap the bytes array;
         */
        static Block swapIfLe(Block b) {
            if (ByteOrder.littleEndian()) {
                return new Block(
                        Longs.fromByteArray(bswap64(b.l)),
                        Longs.fromByteArray(bswap64(b.r))
                );
            } else {
                return b;
            }
        }

        static Block genOffset(long[] ktopStr, int bot) {
            Block rval = zeroBlock();
            if (bot != 0) {
                rval.l = (ktopStr[0] << bot) | (ktopStr[1] >> (64 - bot));
                rval.r = (ktopStr[1] << bot) | (ktopStr[2] >> (64 - bot));
            } else {
                rval.l = ktopStr[0];
                rval.r = ktopStr[1];
            }
            return swapIfLe(rval);
        }

        static boolean unequalBlocks(Block x, Block y) {
            return (((x).l ^ (y).l) | ((x).r ^ (y).r)) != 0;
        }
    }

    public static class AeCtx {
        static final String ALGORITHM = "AES";
        static final String AES_TYPE = "AES/ECB/NoPadding";
        static final int L_TABLE_SIZE = 12;

        final Block[] l = new Block[L_TABLE_SIZE];
        final long[] ktopStr = new long[3];

        Block offset;
        Block checksum;
        Block lstar;
        Block ldollor;
        Block adCheckSum;
        Block adOffset;
        Block cachedTop;
        int adBlocksProcessed;
        int blocksProcessed;
        Cipher encryptCipher;
        Cipher decryptCipher;

        void setCipher(byte[] key) throws Exception {
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);

            encryptCipher = Cipher.getInstance(AES_TYPE);
            encryptCipher.init(Cipher.ENCRYPT_MODE, keySpec);

            decryptCipher = Cipher.getInstance(AES_TYPE);
            decryptCipher.init(Cipher.DECRYPT_MODE, keySpec);
        }

        byte[] encrypt(byte[] origin) throws Exception {
            return encryptCipher.doFinal(origin);
        }

        public AeCtx() {
        }
    }

    public static int aeInit(AeCtx ctx, byte[] key, int keyLen, int nonceLen, int tagLen) {
        if (keyLen != OCB_KEY_LEN) {
            return AE_NOT_SUPPORTED;
        }
        if (nonceLen != 12) {
            return AE_NOT_SUPPORTED;
        }
        try {
            int i;
            Block tmpBlk;

            ctx.setCipher(key);

            ctx.cachedTop = zeroBlock();
            ctx.checksum = zeroBlock();
            ctx.adBlocksProcessed = 0;

            ctx.lstar = zeroBlock();
            ctx.lstar.fromBytes(ctx.encrypt(ctx.cachedTop.getBytes()));

            tmpBlk = swapIfLe(ctx.lstar);
            tmpBlk.doubleBlock();
            ctx.ldollor = swapIfLe(tmpBlk);
            tmpBlk.doubleBlock();
            ctx.l[0] = swapIfLe(tmpBlk);
            for (i = 1; i < AeCtx.L_TABLE_SIZE; i++) {
                tmpBlk.doubleBlock();
                ctx.l[i] = swapIfLe(tmpBlk);
            }
        } catch (Exception e) {
            return AE_INVALID;
        }
        return AE_SUCCESS;
    }

    public static int aeEncrypt(
            AeCtx ctx,
            byte[] nonce,
            byte[] plaintext,
            int plaintextLen,
            byte[] ad,
            long adLen,
            byte[] ciphertext,
            byte[] tag,
            int finalize
    ) {
        // todo: fulfill
        return plaintextLen + 16;
    }

    private static Block genOffsetFromNonce(AeCtx ctx, byte[] nonce) {
        int idx;
        byte[] bytes16 = new byte[16];
        int[] tmp = new int[4];
        tmp[0] = ByteOrder.littleEndian() ? 0x01000000 : 0x00000001;
        byte[] bytes4 = new byte[4];
        for (int i = 1; i <= 3; i++) {
            System.arraycopy(nonce, (i - 1) * 4, bytes4, 0, 4);
            tmp[i] = Ints.fromByteArray(bytes4);
        }

        for (int i = 0; i < tmp.length; i++) {
            byte[] bytes = Ints.toByteArray(tmp[i]);
            System.arraycopy(bytes, 0, bytes16, i * 4, 4);
        }

        idx = bytes16[15] & 0x3f;
        bytes16[15] = (byte) (bytes16[15] & 0xc0);

        Block tmpBlk = zeroBlock();
        tmpBlk.fromBytes(bytes16);

        if (Block.unequalBlocks(tmpBlk, ctx.cachedTop)) {
            // todo: fulfill
        }

        return Block.genOffset(ctx.ktopStr, idx);
    }
}
