package com.toocol.ssh.core.mosh.core.statesnyc;

import java.util.Objects;

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

    protected final UserEventType type;

    public abstract String name();

    public static final class Resize extends UserEvent {

        private final int width;
        private final int height;

        public Resize(int width, int height) {
            super(UserEventType.RESIZE_TYPE);
            this.width = width;
            this.height = height;
        }

        @Override
        public String name() {
            return "Resize";
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Resize resize = (Resize) o;
            if (!type.equals(resize.type)) {
                return false;
            }
            return width == resize.width && height == resize.height;
        }

        @Override
        public int hashCode() {
            return Objects.hash(width, height);
        }
    }

}
