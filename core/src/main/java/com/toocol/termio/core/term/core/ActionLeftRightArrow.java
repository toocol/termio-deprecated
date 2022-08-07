package com.toocol.termio.core.term.core;

import com.toocol.termio.utilities.event.CharEvent;
import com.toocol.termio.utilities.utils.CharUtil;

import java.lang.module.FindException;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:01
 */
public final class ActionLeftRightArrow extends TermCharAction {
    private static final HistoryOutputInfoQuickSwitchHelper historyOutputInfoQuickSwitchHelper = HistoryOutputInfoQuickSwitchHelper.getInstance();
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.LEFT_ARROW, CharEvent.RIGHT_ARROW};
    }

    @Override
    public boolean act(Term term, CharEvent charEvent, char inChar) {

        if (term.status.equals(TermStatus.HISTORY_OUTPUT)) {
            int index = 0;
            if (inChar == CharUtil.LEFT_ARROW) {
                historyOutputInfoQuickSwitchHelper.leftInformation(index);
                index--;
            } else if (inChar == CharUtil.RIGHT_ARROW) {
                historyOutputInfoQuickSwitchHelper.rightInformation(index);
                index++;
            }
            return false;
        }
        int cursorX = term.getCursorPosition()[0];
        if (inChar == CharUtil.LEFT_ARROW) {
            if (cursorX > Term.getPromptLen()) {
                term.cursorLeft();
                term.executeCursorOldX.getAndDecrement();
            }
        } else {
            if (cursorX < (term.lineBuilder.length() + Term.getPromptLen())) {
                term.cursorRight();
                term.executeCursorOldX.getAndIncrement();
            }
        }
        return false;
    }
}
