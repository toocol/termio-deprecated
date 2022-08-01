package com.toocol.termio.core.term.core;

import com.toocol.termio.utilities.anis.Printer;
import com.toocol.termio.utilities.event.CharEvent;
import com.toocol.termio.utilities.utils.CharUtil;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:03
 */
public final class ActionBackspace extends TermCharAction {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.BACKSPACE};
    }

    @Override
    public boolean act(Term term, CharEvent charEvent, char inChar) {
        int[] cursorPosition = term.getCursorPosition();
        if (cursorPosition[0] == Term.getPromptLen()) {
            Printer.bel();
            return false;
        }
        char deleteChar;
        if (cursorPosition[0] < term.lineBuilder.length() + Term.getPromptLen()) {
            int index = cursorPosition[0] - Term.getPromptLen() - 1;
            deleteChar = term.lineBuilder.charAt(index);
            term.lineBuilder.deleteCharAt(index);
        } else {
            deleteChar = term.lineBuilder.charAt(term.lineBuilder.length() - 1);
            term.lineBuilder.deleteCharAt(term.lineBuilder.length() - 1);
        }
        term.executeCursorOldX.getAndUpdate(prev -> {
            if (CharUtil.isChinese(deleteChar)) {
                int val = prev - 2;
                return Math.max(val, Term.getPromptLen());
            } else {
                return --prev;
            }
        });
        return false;
    }
}
