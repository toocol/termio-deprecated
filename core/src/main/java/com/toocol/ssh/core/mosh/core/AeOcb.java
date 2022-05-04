package com.toocol.ssh.core.mosh.core;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static com.toocol.ssh.core.mosh.core.ByteOrder.*;

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

    public static class Block {
        private long l;
        private long r;

        private Block(long l, long r) {
            this.l = l;
            this.r = r;
        }

        private static Block xorBlock(Block x, Block y) {
            x.l ^= y.l;
            x.r ^= y.r;
            return x;
        }

        private static Block zeroBlock() {
            return new Block(0, 0);
        }

        /**
         * Java is little-endian by default;
         * Transfer little-endian long to big-endian long;
         */
        private static Block swapLe(Block b) {
            return new Block(
                    longBe(htoBe64(b.l)),
                    longBe(htoBe64(b.r))
            );
        }

        private static Block genOffset(long[] ktopStr, long bot) {
            Block rval = zeroBlock();
            //TODO: fulfill
            return swapLe(rval);
        }
    }

    public static class AeCtx {
        private static final String ALGORITHM = "AES";
        private static final String AES_TYPE = "AES/ECB/PKCS5Padding";
        private static final int L_TABLE_SIZE = 12;

        private final Block[] l = new Block[L_TABLE_SIZE];
        private final long[] ktopStr = new long[3];

        private Block offset;
        private Block checksum;
        private Block lstar;
        private Block adCheckSum;
        private Block adOffset;
        private Block cachedTop;
        private int adBlocksProcessed;
        private int blocksProcessed;
        private Cipher cipher;

        private void init(byte[] key) throws Exception{
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
            cipher = Cipher.getInstance(AES_TYPE);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        }

        private byte[] encrypt(byte[] origin) throws Exception {
            return cipher.doFinal(origin);
        }

        public AeCtx() {
        }
    }

    public static int aeInit(AeCtx aeCtx, byte[] key, int keyLen, int nonceLen, int tagLen) {
        if (nonceLen != 12) {
            return AE_NOT_SUPPORTED;
        }
        try {
            aeCtx.init(key);
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
}
