package com.toocol.ssh.core.mosh.core.network;

import com.toocol.ssh.core.mosh.core.crypto.ByteOrder;
import com.toocol.ssh.core.mosh.core.crypto.Crypto;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/30 17:29
 * @version: 0.0.1
 */
public final class MoshPacket {
    static final int ADDED_BYTES = 8 /* seqno/nonce */ + 4 /* timestamp */;

    public enum Direction {
        TO_SERVER(0),
        TO_CLIENT(1);
        private final long idx;

        Direction(long idx) {
            this.idx = idx;
        }
    }

    private static final long DIRECTION_MASK = 1L << 63;
    private static final long SEQUENCE_MASK = ~DIRECTION_MASK;

    private final long seq;
    private final Direction direction;
    private final byte[] payload;
    private final short timestamp;
    private final short timestampReply;

    public MoshPacket(byte[] payload, Direction direction, short timestamp, short timestampReply) {
        this.seq = Crypto.unique();
        this.payload = payload;
        this.direction = direction;
        this.timestamp = timestamp;
        this.timestampReply = timestampReply;
    }

    public MoshPacket(Crypto.Message message) {
        this.seq = message.nonce.val() & SEQUENCE_MASK;
        this.direction = ((message.nonce.val() & DIRECTION_MASK) == 0) ? Direction.TO_SERVER : Direction.TO_CLIENT;
        this.timestamp = message.getTimestamp();
        this.timestampReply = message.getTimestampReply();
        this.payload = new byte[message.text.length - 4];
        System.arraycopy(message.text, 4, payload, 0, message.text.length - 4);
    }

    public Crypto.Message toMessage() {
        long directionSeq = (direction.idx << 63) | (seq & SEQUENCE_MASK);

        byte[] text = new byte[4 + payload.length];
        System.arraycopy(timestampsMerge(), 0, text, 0, 4);
        System.arraycopy(payload, 0, text, 4, payload.length);

        return new Crypto.Message(new Crypto.Nonce(directionSeq), text);
    }

    private byte[] timestampsMerge() {
        byte[] timestampBytes = ByteOrder.htoBe16(timestamp);
        byte[] timestampReplyBytes = ByteOrder.htoBe16(timestampReply);
        byte[] target = new byte[4];
        System.arraycopy(timestampBytes, 0, target, 0, 2);
        System.arraycopy(timestampReplyBytes, 0, target, 2, 2);
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoshPacket packet = (MoshPacket) o;
        return seq == packet.seq && timestamp == packet.timestamp && timestampReply == packet.timestampReply && direction == packet.direction && Arrays.equals(payload, packet.payload);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(seq, direction, timestamp, timestampReply);
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }

    public long getSeq() {
        return seq;
    }

    public Direction getDirection() {
        return direction;
    }

    public byte[] getPayload() {
        return payload;
    }

    public short getTimestamp() {
        return timestamp;
    }

    public short getTimestampReply() {
        return timestampReply;
    }
}
