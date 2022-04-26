package com.toocol.ssh.core.term.core;

import com.toocol.ssh.utilities.event.CharEvent;
import com.toocol.ssh.utilities.utils.CharUtil;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:01
 */
public final class ActionLeftRightArrow extends TermCharAction {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.LEFT_ARROW, CharEvent.RIGHT_ARROW};
    }

    @Override
    public boolean act(Term term, CharEvent charEvent, char inChar) {
        int cursorX = term.getCursorPosition()._1();
        if (inChar == CharUtil.LEFT_ARROW) {
            if (cursorX > Term.PROMPT.length() + 4) {
                term.cursorLeft();
                term.executeCursorOldX.getAndDecrement();
            }
        } else {
            if (cursorX < (term.lineBuilder.length() + Term.PROMPT.length() + 4)) {
                term.cursorRight();
                term.executeCursorOldX.getAndIncrement();
            }
        }
        return false;
    }
}
