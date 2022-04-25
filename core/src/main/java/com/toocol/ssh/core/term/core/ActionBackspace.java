package com.toocol.ssh.core.term.core;

import com.toocol.ssh.utilities.event.CharEvent;
import com.toocol.ssh.utilities.utils.CharUtil;
import com.toocol.ssh.utilities.utils.Tuple2;

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
        Tuple2<Integer, Integer> cursorPosition = term.getCursorPosition();
        if (cursorPosition._1() == Term.PROMPT.length()) {
            Printer.voice();
            return false;
        }
        char deleteChar;
        if (cursorPosition._1() < term.lineBuilder.length() + Term.PROMPT.length()) {
            int index = cursorPosition._1() - Term.PROMPT.length() - 1;
            deleteChar = term.lineBuilder.charAt(index);
            term.lineBuilder.deleteCharAt(index);
        } else {
            deleteChar = term.lineBuilder.charAt(term.lineBuilder.length() - 1);
            term.lineBuilder.deleteCharAt(term.lineBuilder.length() - 1);
        }
        term.executeCursorOldX.getAndUpdate(prev -> {
            if (CharUtil.isChinese(deleteChar)) {
                int val = prev - 2;
                return Math.max(val, Term.PROMPT.length());
            } else {
                return --prev;
            }
        });
        return false;
    }
}
