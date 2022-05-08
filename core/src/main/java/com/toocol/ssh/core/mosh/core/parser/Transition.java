package com.toocol.ssh.core.mosh.core.parser;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/8 20:26
 * @version: 0.0.1
 */
public class Transition {
    public Action action;
    public State nextState;

    public Transition(Action action, State nextState) {
        this.action = action;
        this.nextState = nextState;
    }
}
