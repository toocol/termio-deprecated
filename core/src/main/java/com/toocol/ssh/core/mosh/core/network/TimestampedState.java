package com.toocol.ssh.core.mosh.core.network;

import com.toocol.ssh.core.mosh.core.parser.State;

import java.util.Objects;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/8 17:40
 * @version: 0.0.1
 */
public class TimestampedState {

    public long timestamp;
    public long num;
    public State state;

    public TimestampedState(long timestamp, long num, State state) {
        this.timestamp = timestamp;
        this.num = num;
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimestampedState that = (TimestampedState) o;
        return timestamp == that.timestamp && num == that.num;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, num);
    }
}
