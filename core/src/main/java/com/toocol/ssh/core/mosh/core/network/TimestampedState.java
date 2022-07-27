package com.toocol.ssh.core.mosh.core.network;

import com.toocol.ssh.core.mosh.core.statesnyc.State;

import java.util.Objects;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/8 17:40
 * @version: 0.0.1
 */
public final class TimestampedState<T extends State> {

    public long timestamp;
    public long num;
    public transient T state;

    public TimestampedState(long timestamp, long num, T state) {
        this.timestamp = timestamp;
        this.num = num;
        this.state = state;
    }

    public TimestampedState() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimestampedState<?> that = (TimestampedState<?>) o;
        return timestamp == that.timestamp && num == that.num && Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, num);
    }
}
