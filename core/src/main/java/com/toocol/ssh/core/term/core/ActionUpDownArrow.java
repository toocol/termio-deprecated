package com.toocol.ssh.core.term.core;

import com.toocol.ssh.utilities.event.CharEvent;
import com.toocol.ssh.utilities.utils.CharUtil;

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
    public boolean act(Term term, CharEvent charEvent, char inChar) {
        if (inChar == CharUtil.UP_ARROW) {
            if (!term.historyHelper.isStart()) {
                if (term.lineBuilder.toString().length() != 0) {
                    term.historyHelper.pushToDown(term.lineBuilder.toString());
                }
            }
            String up = term.historyHelper.up();
            if (up != null) {
                term.lineBuilder.delete(0, term.lineBuilder.length()).append(up);
            }
        } else {
            String down = term.historyHelper.down();
            if (down != null) {
                term.lineBuilder.delete(0, term.lineBuilder.length()).append(down);
            }
        }
        term.executeCursorOldX.set(term.lineBuilder.length() + Term.PROMPT.length());
        return false;
    }
}
