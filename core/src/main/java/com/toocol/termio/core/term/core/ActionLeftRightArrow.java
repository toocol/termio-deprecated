package com.toocol.termio.core.term.core;

import com.toocol.termio.utilities.event.CharEvent;



/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:01
 */
public final class ActionLeftRightArrow extends TermCharAction {
    private static final HistoryOutputInfoHelper historyOutputInfoHelper = HistoryOutputInfoHelper.getInstance();

    public ActionLeftRightArrow() {
    }

    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.LEFT_ARROW, CharEvent.RIGHT_ARROW};
    }

    public boolean act(Term term, CharEvent charEvent, char inChar) {
        if (Term.status.equals(TermStatus.HISTORY_OUTPUT)) {
            if (inChar == '\udddd') {
                historyOutputInfoHelper.pageLeft();
            } else if (inChar == '쳌') {
                historyOutputInfoHelper.pageRight();
            }

            return false;
        } else {
            int cursorX = term.getCursorPosition()[0];
            if (inChar == '\udddd') {
                if (cursorX > Term.getPromptLen()) {
                    term.cursorLeft();
                    term.executeCursorOldX.getAndDecrement();
                }
            } else if (cursorX < term.lineBuilder.length() + Term.getPromptLen()) {
                term.cursorRight();
                term.executeCursorOldX.getAndIncrement();
            }

            return false;
        }
    }
}
