package com.toocol.ssh.common.utils;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 12:14
 */
public class SnowflakeGuidGenerator {
    public static final int LONG_BITS = 64;
    public static final int SERVER_ID_BITS = 2;
    public static final int SEQUENCE_BITS = 16;
    public static final int TIMESTAMP_SHIFT_BITS = SEQUENCE_BITS + SERVER_ID_BITS;
    public static final int SERVER_ID_SHIFT_BITS = SEQUENCE_BITS;
    public static final int SEQUENCE_REDUCTION_SHIFT_BITS = LONG_BITS - SEQUENCE_BITS;

    private static final long MAX_SEQUENCE_PER_MILLIS = -1L >>> (LONG_BITS - SEQUENCE_BITS);

    private long sequence = 1;
    private long lastTimestamp = 0;

    public long nextId() {
        Tuple<Long, Long> tuple = sequenceOf();
        return (tuple._1() << TIMESTAMP_SHIFT_BITS)
                | (1 << SERVER_ID_SHIFT_BITS)
                | tuple._2();
    }

    /**
     * @return Tuple<时间戳, 序列号>
     */
    private synchronized Tuple<Long, Long> sequenceOf() {
        long timestamp = timeGen();

        if (timestamp == lastTimestamp) {
            sequence++;
            if (sequence > MAX_SEQUENCE_PER_MILLIS) {
                timestamp = tilNextMillis(timestamp);
            }
        }
        if (timestamp > lastTimestamp) {
            sequence = 1;
        }

        lastTimestamp = timestamp;
        return new Tuple<>(timestamp, sequence);
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }
}
