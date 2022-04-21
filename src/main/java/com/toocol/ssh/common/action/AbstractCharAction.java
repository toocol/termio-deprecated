package com.toocol.ssh.common.action;

import com.toocol.ssh.common.event.CharEvent;
import com.toocol.ssh.core.shell.core.Shell;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 18:23
 */
public abstract class AbstractCharAction {

    /**
     * subscribe the interested char event;
     *
     * @return interested char event
     */
    public abstract CharEvent[] watch();


    public abstract void act(Shell shell, CharEvent charEvent, char inChar);

}
