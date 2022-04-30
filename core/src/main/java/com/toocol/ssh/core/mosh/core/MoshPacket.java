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

    public MoshPacket(String payload, Direction direction) {
        this.payload = payload;
        this.direction = direction;
    }

    Crypto.Message toMessage() {
        long directionSeq = (direction.idx << 63) | (seq & SEQUENCE_MASK);
        String timestamps = "";
        return new Crypto.Message(new Crypto.Nonce(directionSeq), timestamps + payload);
    }

    public byte[] getBytes() {
        return Crypto.encrypt(toMessage());
    }
}
