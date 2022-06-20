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
    public byte[] diffFrom(UserStream existing) {
        Iterator<UserEvent> iterator = existing.actions.iterator();
        Iterator<UserEvent> myIt = actions.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            myIt.next();
        }

        UserInputPB.UserMessage.Builder output = UserInputPB.UserMessage.newBuilder();

        while (myIt.hasNext()) {
            UserEvent next = myIt.next();
            switch (next.type()) {
                case RESIZE_TYPE -> {
                    UserEvent.Resize resizeEvent = next.as();
                    UserInputPB.Instruction.Builder instructionBuilder = UserInputPB.Instruction.newBuilder();
                    UserInputPB.ResizeMessage.Builder resizeBuilder = UserInputPB.ResizeMessage.newBuilder();
                    resizeBuilder.setWidth(resizeEvent.getWidth());
                    resizeBuilder.setHeight(resizeEvent.getHeight());
                    instructionBuilder.setExtension(UserInputPB.resize, resizeBuilder.build());
                    output.addInstruction(instructionBuilder);
                }
                case INITIALISE_TYPE -> {

                }
            }
        }
        return output.build().toByteArray();
    }

    @Override
    public void pushBack(UserEvent event) {
        actions.offer(event);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof UserStream us)) {
            return false;
        }
        if (this.actions.size() != us.actions.size()) {
            return false;
        }

        Iterator<UserEvent> thisIterator = actions.iterator();
        Iterator<UserEvent> usIterator = us.actions.iterator();
        while (thisIterator.hasNext()) {
            UserEvent thisNext = thisIterator.next();
            UserEvent usNext = usIterator.next();
            if (!thisNext.equals(usNext)) {
                return false;
            }
        }
        return true;
    }

}
