package com.toocol.ssh.core.mosh.core.statesnyc;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/8 19:43
 * @version: 0.0.1
 */
public abstract class State<T extends State<?>> {

    public abstract void subtract(T prefix);

    public abstract String diffFrom(T existing);

}