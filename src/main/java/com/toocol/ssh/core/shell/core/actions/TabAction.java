package com.toocol.ssh.core.shell.core.actions;

import com.toocol.ssh.common.action.AbstractCharAction;
import com.toocol.ssh.common.event.CharEvent;
import com.toocol.ssh.core.shell.core.Shell;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 20:45
 */
public class TabAction extends AbstractCharAction {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.TAB};
    }

    @Override
    public void act(Shell shell, CharEvent charEvent, char inChar) {

    }
}
