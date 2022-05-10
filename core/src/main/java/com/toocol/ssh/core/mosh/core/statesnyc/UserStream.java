package com.toocol.ssh.core.mosh.core.statesnyc;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * user.h:UserStream
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/11 0:30
 * @version: 0.0.1
 */
public class UserStream extends State{

    private final Deque<UserEvent> actions = new ArrayDeque<>();

    @Override
    public <T extends State> void subtract(T prefix) {

    }

    @Override
    public boolean equals(Object obj) {
        // todo: equals method need to override
        return super.equals(obj);
    }
}
