package com.toocol.termio.core.term.core;

import com.toocol.termio.utilities.event.CharEvent;
import com.toocol.termio.utilities.utils.CharUtil;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 17:59
 */
public final class ActionUpDownArrow extends TermCharAction {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.UP_ARROW, CharEvent.DOWN_ARROW};
    }

    @Override
    public boolean actOnConsole(Term term, CharEvent charEvent, char inChar) {
        if (inChar == CharUtil.UP_ARROW) {
            if (!term.historyCmdHelper.isStart()) {
                if (term.lineBuilder.toString().length() != 0) {
                    term.historyCmdHelper.pushToDown(term.lineBuilder.toString());
                }
            }
            String up = term.historyCmdHelper.up();
            if (up != null) {
                term.lineBuilder.delete(0, term.lineBuilder.length()).append(up);
            }
        } else {
            String down = term.historyCmdHelper.down();
            if (down != null) {
                term.lineBuilder.delete(0, term.lineBuilder.length()).append(down);
            }
        }
        term.executeCursorOldX.set(term.lineBuilder.length() + Term.getPromptLen());
        return false;
    }

    @Override
    public boolean actOnDesktop(Term term, CharEvent charEvent, char inChar) {
        return false;
    }
}
