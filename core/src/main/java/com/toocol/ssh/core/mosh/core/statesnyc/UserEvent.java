package com.toocol.ssh.core.mosh.core.statesnyc;

/**
 * user.h:UserEvent
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/11 0:29
 * @version: 0.0.1
 */
public abstract class UserEvent {
    public UserEvent(UserEventType type) {
        this.type = type;
    }

    public enum UserEventType {
        USER_BYTE_TYPE(0),
        RESIZE_TYPE(1)
        ;
        public final int idx;

        UserEventType(int idx) {
            this.idx = idx;
        }
    }

    private final UserEventType type;

    public abstract String name();

    public static final class Resize extends UserEvent {
        public Resize() {
            super(UserEventType.RESIZE_TYPE);
        }

        @Override
        public String name() {
            return "Resize";
        }

    }

}
