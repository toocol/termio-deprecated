package com.toocol.termio.utilities.utils;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 12:14
 */
public class SnowflakeGuidGenerator {
    public static final int LONG_BITS = 64;
    public static final int UNIQUE_ID_BITS = 2;
    public static final int SEQUENCE_BITS = 16;
    public static final int TIMESTAMP_SHIFT_BITS = SEQUENCE_BITS + UNIQUE_ID_BITS;
    public static final int UNIQUE_ID_SHIFT_BITS = SEQUENCE_BITS;
    private static final long MAX_SEQUENCE_PER_MILLIS = -1L >>> (LONG_BITS - SEQUENCE_BITS);
    private static SnowflakeGuidGenerator instance;
    private long sequence = 1;
    private long lastTimestamp = 0;

    private SnowflakeGuidGenerator() {
    }

    public static synchronized SnowflakeGuidGenerator getInstance() {
        if (instance == null) {
            instance = new SnowflakeGuidGenerator();
        }
        return instance;
    }

    public long nextId() {
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
        return (timestamp << TIMESTAMP_SHIFT_BITS)
                | (1 << UNIQUE_ID_SHIFT_BITS)
                | sequence;
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
