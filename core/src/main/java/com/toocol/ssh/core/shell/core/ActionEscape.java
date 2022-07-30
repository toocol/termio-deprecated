package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.utilities.event.CharEvent;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/7/29 22:46
 * @version: 0.0.1
 */
public class ActionEscape extends ShellCharAction {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.ESCAPE};
    }

    @Override
    public boolean act(Shell shell, CharEvent charEvent, char inChar) {
        if (!shell.status.equals(Shell.Status.QUICK_SWITCH)) {
            return false;
        }
        shell.quickSwitchHelper.quit();
        return true;
    }
}
