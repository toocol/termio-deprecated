package com.toocol.ssh.core.mosh.core.crypto;

import com.toocol.ssh.utilities.execeptions.CryptoException;
import com.toocol.ssh.utilities.utils.MessageBox;

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
public final class Crypto {

    public static class Nonce {
        public static final int NONCE_LEN = 12;

        private final byte[] bytes = new byte[NONCE_LEN];

        public Nonce(long directionSeq) {
            System.arraycopy(ByteOrder.htoBe64(directionSeq), 0, this.bytes, 4, 8);
        }

        public Nonce(byte[] bytes, int len) {
            if (len != 8) {
                throw new CryptoException("Nonce representation must be 8 octets long.");
            }

            System.arraycopy(bytes, 0, this.bytes, 4, 8);
        }

        public long val() {
            byte[] longBytes = new byte[8];
            System.arraycopy(this.bytes, 4, longBytes, 0, 8);
            return ByteOrder.be64toh(longBytes);
        }

        public byte[] data() {
            return bytes;
        }

        public byte[] ccBytes() {
            byte[] cc = new byte[8];
            System.arraycopy(bytes, 4, cc, 0, 8);
            return cc;
        }
    }

    public static class Message {
        public Nonce nonce;
        public byte[] text;

        public Message() {
        }

        public Message(Nonce nonce, byte[] text) {
            this.nonce = nonce;
            this.text = text;
        }

        public Message resetData(Nonce nonce, byte[] text) {
            this.nonce = nonce;
            this.text = text;
            return this;
        }

        public short getTimestamp() {
            return ByteOrder.be16toh(new byte[]{text[0], text[1]});
        }

        public short getTimestampReply() {
            return ByteOrder.be16toh(new byte[]{text[2], text[3]});
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

        AeOcb.AeCtx ctx;
        long blocksEncrypted;

        AlignedBuffer plaintextBuffer;
        AlignedBuffer ciphertextBuffer;
        AlignedBuffer nonceBuffer;

        public Session(Base64Key key) {
            this.blocksEncrypted = 0;
            this.plaintextBuffer = new AlignedBuffer(RECEIVE_MTU);
            this.ciphertextBuffer = new AlignedBuffer(RECEIVE_MTU);
            this.nonceBuffer = new AlignedBuffer(Nonce.NONCE_LEN);
            this.ctx = new AeOcb.AeCtx();

            if (AeOcb.AE_SUCCESS != AeOcb.aeInit(this.ctx, key.key, 16, 12, 16)) {
                throw new CryptoException("Could not initialize AES-OCB context.");
            }
        }

        public byte[] encrypt(Message plainText) {
            int ptLen = plainText.text.length;
            int ciphertextLen = ptLen + 16;

            assert ciphertextLen * 2 <= ciphertextBuffer.len;
            assert ptLen * 2 <= plaintextBuffer.len;

            System.arraycopy(plainText.text, 0, plaintextBuffer.data, 0, plainText.text.length);
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

            String text = new String(
                    ciphertextBuffer.data,
                    0,
                    ciphertextLen,
                    StandardCharsets.UTF_8
            );

            byte[] bytes = new byte[8 + ciphertextLen];
            System.arraycopy(plainText.nonce.ccBytes(), 0, bytes, 0, 8);
            System.arraycopy(ciphertextBuffer.data, 0, bytes, 8, ciphertextLen);

            return bytes;
        }

        public Message decrypt(byte[] str, int len, Message message) {
            if (len < 24) {
                throw new CryptoException("Ciphertext must contain nonce and tag.");
            }

            int bodyLen = len - 8;
            int ptLen = bodyLen - 16;

            if (ptLen < 0) {
                MessageBox.setExitMessage("Mosh error, invalid message length.");
                System.exit(-1);
            }

            assert bodyLen <= ciphertextBuffer.len;
            assert ptLen <= plaintextBuffer.len;

            Nonce nonce = new Nonce(str, 8);
            System.arraycopy(str, 8, ciphertextBuffer.data, 0, bodyLen);
            System.arraycopy(nonce.data(), 0, nonceBuffer.data, 0, Nonce.NONCE_LEN);

            if (ptLen != AeOcb.aeDecrypt(ctx, /* ctx */
                    nonceBuffer.data,      /* nonce */
                    ciphertextBuffer.data, /* ct */
                    bodyLen,               /* ct_len */
                    null,                  /* ad */
                    0,                     /* ad_len */
                    plaintextBuffer.data,  /* pt */
                    null,                  /* tag */
                    1)) {                  /* final */
                throw new CryptoException("Packet failed integrity check.");
            }

            byte[] text = new byte[ptLen];
            System.arraycopy(plaintextBuffer.data, 0, text, 0, ptLen);
            return message.resetData(nonce, text);
        }
    }

    private static long counter = 0;

    public synchronized static long unique() {
        return counter++;
    }

}
