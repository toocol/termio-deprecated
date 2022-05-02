package com.toocol.ssh.core.mosh.core;

/**
 * ae.h/ocb.cc
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/2 22:28
 * @version: 0.0.1
 */
public class AeOcb {
    public static class Block {
        private long l;
        private long r;
        // todo: fulfill
    }

    public static class AeCtx {
        // todo: fulfill
    }

    public static AeCtx aeInit(byte[] key, int keyLen, int nonceLen, int tagLen) {
        // todo: fulfill
        return new AeCtx();
    }

    public static int aeEncrypt(
            AeCtx ctx,
            byte[] nonce,
            byte[] plaintext,
            long plaintextLen,
            byte[] ad,
            long adLen,
            byte[] ciphertext,
            byte[] tag,
            int finalize
    ) {
        // todo: fulfill
        return 0;
    }
}
