package com.toocol.ssh.core.mosh.core;

import com.toocol.ssh.utilities.execeptions.CryptoException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * crypto.cc
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/30 17:26
 * @version: 0.0.1
 */
@SuppressWarnings("all")
public class Crypto {

    public static class Nonce {
        public static final int NONCE_LEN = 12;

        private final byte[] bytes = new byte[NONCE_LEN];

        public Nonce(long directionSeq) {
            System.arraycopy(ByteOrder.htoBe64(directionSeq), 0, this.bytes, 4, 8);
        }

        public byte[] data() {
            return bytes;
        }

        public String ccStr() {
            byte[] cc = new byte[8];
            System.arraycopy(bytes, 4, cc, 0, 8);
            return new String(cc);
        }
    }

    public static class Message {
        public final Nonce nonce;
        public final String text;

        public Message(Nonce nonce, String text) {
            this.nonce = nonce;
            this.text = text;
        }

        public byte[] data() {
            return text.getBytes(StandardCharsets.UTF_8);
        }
    }

    public static class Base64Key {
        private static final int KEY_LEN = 16;
        private final byte[] key;

        public Base64Key(String printableKey) {
            if (printableKey.length() != 22) {
                throw new CryptoException("Key must be 22 letters long.");
            }

            String base64 = printableKey + "==";
            key = Base64.getDecoder().decode(base64);
            if (key.length != KEY_LEN) {
                throw new CryptoException("Key must represent 16 octets.");
            }

            if (!printableKey.equals(printableKey())) {
                throw new CryptoException("Base64 key was not encoded 128-bit key.");
            }
        }

        public String printableKey() {
            byte[] base64 = Base64.getEncoder().encode(key);

            if (base64[23] != '=' || base64[22] != '=') {
                throw new CryptoException("Unexpected output from base64_encode: " + new String(base64));
            }

            return new String(base64, 0, 22);
        }
    }

    public static class AlignedBuffer {
        private int len;
        private byte[] data;

        public AlignedBuffer(int len) {
            this.len = len;
            this.data = new byte[len];
        }
    }

    public static class Session {
        public static final int RECEIVE_MTU = 2048;
        public static final int ADDED_BYTES = 16;

        Base64Key key;
        AeOcb.AeCtx ctx;
        AlignedBuffer ctxBuf;
        long blocksEncrypted;

        AlignedBuffer plaintextBuffer;
        AlignedBuffer ciphertextBuffer;
        AlignedBuffer nonceBuffer;

        Session(Base64Key key) {
            this.key = key;
            this.blocksEncrypted = 0;
            this.plaintextBuffer = new AlignedBuffer(RECEIVE_MTU);
            this.ciphertextBuffer = new AlignedBuffer(RECEIVE_MTU);
            this.nonceBuffer = new AlignedBuffer(Nonce.NONCE_LEN);
            this.ctx = new AeOcb.AeCtx();

            if (AeOcb.AE_SUCCESS != AeOcb.aeInit(this.ctx, key.key, 16, 12, 16)) {
                throw new CryptoException("Could not initialize AES-OCB context.");
            }
        }

        byte[] encrypt(Message plainText) {
            int ptLen = plainText.text.length();
            int ciphertextLen = ptLen + 16;

            assert ciphertextLen <= ciphertextBuffer.len;
            assert ptLen <= plaintextBuffer.len;

            System.arraycopy(plainText.data(), 0, plaintextBuffer.data, 0, plainText.data().length);
            System.arraycopy(plainText.nonce.data(), 0, nonceBuffer.data, 0, Nonce.NONCE_LEN);

            if (ciphertextLen != AeOcb.aeEncrypt(
                    ctx,
                    nonceBuffer.data,
                    plaintextBuffer.data,
                    ptLen,
                    null,
                    0,
                    ciphertextBuffer.data,
                    null,
                    1
            )) {
                throw new CryptoException("aeEncrypt() returned error.");
            }

            blocksEncrypted += ptLen >> 4;
            if ((ptLen & 0xF) > 0) {
                blocksEncrypted++;
            }

            if ((blocksEncrypted >> 47) > 0) {
                throw new CryptoException("Encrypted 2^47 blocks.");
            }

            String text = new String(ciphertextBuffer.data, 0, ciphertextLen);

            return (plainText.nonce.ccStr() + text).getBytes(StandardCharsets.UTF_8);
        }
    }

    private static long counter = 0;

    public synchronized static long unique() {
        return ++counter;
    }

}
