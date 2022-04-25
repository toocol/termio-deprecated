package com.toocol.ssh.utilities.action;

import com.toocol.ssh.utilities.event.CharEvent;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 18:23
 */
public abstract class AbstractCharAction<D extends AbstractDevice> {
    /**
     * subscribe the interested char event;
     *
     * @return interested char event
     */
    public abstract CharEvent[] watch();

    /**
     * @param device    the char event action which belongs to.
     * @param charEvent char input event.
     * @param inChar    the input char.
     * @return true: break the outside loop; false: continue
     */
    public abstract boolean act(D device, CharEvent charEvent, char inChar);
}
