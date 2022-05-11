package com.toocol.ssh.core.mosh.core.statesnyc;

import com.toocol.ssh.core.mosh.core.proto.UserInputPB;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * user.h:UserStream
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/11 0:30
 * @version: 0.0.1
 */
public final class UserStream extends State<UserStream>{

    private final Deque<UserEvent> actions = new ArrayDeque<>();

    @Override
    public void subtract(UserStream prefix) {
        if (this.equals(prefix)) {
            actions.clear();
        }

        for (UserEvent next : prefix.actions) {
            UserEvent peek = actions.peek();
            if (peek != null && peek.equals(next)) {
                actions.poll();
            }
        }
    }

    @Override
    public String diffFrom(UserStream existing) {
        Iterator<UserEvent> iterator = existing.actions.iterator();
        Iterator<UserEvent> myIt = actions.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            myIt.next();
        }

        UserInputPB.UserMessage.Builder output = UserInputPB.UserMessage.newBuilder();

        while (myIt.hasNext()) {
            UserEvent next = myIt.next();
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        // todo: equals method need to override
        return super.equals(obj);
    }

}
