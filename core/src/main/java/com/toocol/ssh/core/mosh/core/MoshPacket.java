package com.toocol.ssh.core.mosh.core;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/30 17:29
 * @version: 0.0.1
 */
public class MoshPacket {
    public enum Direction {
        TO_SERVER(0),
        TO_CLIENT(1)
        ;
        private final long idx;

        Direction(long idx) {
            this.idx = idx;
        }
    }

    private static final long DIRECTION_MASK = 1L << 63;
    private static final long SEQUENCE_MASK = ~DIRECTION_MASK;

    private final long seq = Crypto.unique();
    private final Direction direction;
    private final String payload;
    private final short timestamp;
    private final short timestampReply;

    public MoshPacket(String payload, Direction direction, short timestamp, short timestampReply) {
        this.payload = payload;
        this.direction = direction;
        this.timestamp = timestamp;
        this.timestampReply = timestampReply;
    }

    public byte[] getBytes(String printableKey) {
        return Crypto.encrypt(printableKey, toMessage());
    }

    private Crypto.Message toMessage() {
        long directionSeq = (direction.idx << 63) | (seq & SEQUENCE_MASK);

        String timestamps = new String(timestampsMerge());

        return new Crypto.Message(new Crypto.Nonce(directionSeq), timestamps + payload);
    }

    private byte[] timestampsMerge() {
        byte[] timestampBytes = ByteOrder.htoBe16(timestamp);
        byte[] timestampReplyBytes = ByteOrder.htoBe16(timestampReply);
        byte[] target = new byte[4];
        System.arraycopy(timestampBytes, 0, target, 0, 2);
        System.arraycopy(timestampReplyBytes, 0, target, 2, 2);
        return target;
    }
}
