package com.toocol.ssh.core.term.core;

import com.toocol.ssh.utilities.event.CharEvent;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:04
 */
public final class ActionCtrlU extends TermCharAction {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.CTRL_U};
    }

    @Override
    public boolean act(Term term, CharEvent charEvent, char inChar) {
        term.lineBuilder.delete(0, term.lineBuilder.length());
        term.executeCursorOldX.set(Term.getPromptLen());
        return false;
    }
}
