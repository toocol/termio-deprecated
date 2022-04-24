package com.toocol.ssh.common.action;

import com.toocol.ssh.common.event.CharEvent;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 18:23
 */
public abstract class AbstractCharAction<S extends AbstractShell> {

    /**
     * subscribe the interested char event;
     *
     * @return interested char event
     */
    public abstract CharEvent[] watch();

    /**
     * @param shell     the current shell
     * @param charEvent char input event
     * @param inChar    the input char
     * @return true: break the outside loop; false: continue
     */
    public abstract boolean act(S shell, CharEvent charEvent, char inChar);

    /*
     * reset the action;
     **/
    public static void reset() {
        localLastInputBuffer.delete(0, localLastInputBuffer.length());
        remoteCursorOffset = false;
    }

    /**
     * record the local input string in this read loop.
     */
    protected static final StringBuilder localLastInputBuffer = new StringBuilder();

    /**
     * remote cursor position has changed sign.
     */
    protected static boolean remoteCursorOffset = false;

}
