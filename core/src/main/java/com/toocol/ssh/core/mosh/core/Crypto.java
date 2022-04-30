package com.toocol.ssh.core.mosh.core;

/**
 * crypto.cc
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/30 17:26
 * @version: 0.0.1
 */
public class Crypto {

    public static class Nonce {
        public static final int NONCE_LEN = 12;

        private final byte[] bytes = new byte[NONCE_LEN];

        public Nonce(long directionSeq) {
            System.arraycopy(ByteOrder.htobe64(directionSeq), 0, this.bytes, 4, 8);
        }

    }

    public static class Message {
        private final Nonce nonce;
        private final String text;

        public Message(Nonce nonce, String text) {
            this.nonce = nonce;
            this.text = text;
        }
    }

    public static class Session {
        public static byte[] encrypt(Message plainText) {
            return new byte[1];
        }
    }

    private static long counter = 0;

    public synchronized static long unique() {
        return ++counter;
    }

    public static byte[] encrypt(Message message) {
        return Session.encrypt(message);
    }

}
