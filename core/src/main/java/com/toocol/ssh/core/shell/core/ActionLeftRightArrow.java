package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.utilities.event.CharEvent;
import com.toocol.ssh.utilities.utils.CharUtil;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 20:42
 */
public final class ActionLeftRightArrow extends ShellCharAction {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.LEFT_ARROW, CharEvent.RIGHT_ARROW};
    }

    @Override
    public boolean act(Shell shell, CharEvent charEvent, char inChar) {
        if (shell.status.equals(Shell.Status.QUICK_SWITCH)) {
            return false;
        }
        int cursorX = shell.term.getCursorPosition()[0];
        if (inChar == CharUtil.LEFT_ARROW) {
            if (cursorX > shell.prompt.get().length()) {
                shell.term.cursorLeft();
            }
        } else {
            if (cursorX < (shell.currentPrint.length() + shell.prompt.get().length())) {
                shell.term.cursorRight();
            }
        }
        return false;
    }
}
