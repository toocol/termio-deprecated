package com.toocol.ssh.core.mosh.core.statesnyc;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/8 19:43
 * @version: 0.0.1
 */
public abstract class State<T extends State<?>> {

    public void subtract(T prefix) {}

    public byte[] diffFrom(T existing) {
        return new byte[0];
    }

    public void pushBack(UserEvent event) {}

    public T copy() {
        return null;
    }


    public int actionSize() {
        return 0;
    }

    public void applyString(byte[] diff, long ackNum) {}
}
