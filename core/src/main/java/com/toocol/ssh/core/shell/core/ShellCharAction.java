package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.utilities.action.AbstractCharAction;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:10
 */
public abstract class ShellCharAction extends AbstractCharAction<Shell> {
    /**
     * record the local input string in this read loop.
     */
    protected static final StringBuilder localLastInputBuffer = new StringBuilder();
    /**
     * remote cursor position has changed sign.
     */
    protected static boolean remoteCursorOffset = false;

    /*
     * reset the action;
     **/
    public static void reset() {
        localLastInputBuffer.delete(0, localLastInputBuffer.length());
        remoteCursorOffset = false;
    }
}
