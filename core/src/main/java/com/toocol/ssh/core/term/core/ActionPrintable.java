package com.toocol.ssh.core.term.core;

import com.toocol.ssh.utilities.event.CharEvent;
import com.toocol.ssh.utilities.utils.CharUtil;
import com.toocol.ssh.utilities.utils.Tuple2;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 17:55
 */
public final class ActionPrintable extends TermCharAction {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.ASCII_PRINTABLE, CharEvent.CHINESE_CHARACTER};
    }

    @Override
    public boolean act(Term term, CharEvent charEvent, char inChar) {
        if (term.escapeHelper.isAcceptBracketAfterEscape()) {
            return false;
        }
        if (inChar == CharUtil.SPACE && term.lineBuilder.length() == 0) {
            return false;
        }
        int cursorX = term.executeCursorOldX.get();
        if (cursorX >= term.getWidth() - 1) {
            return false;
        }
        if (cursorX < term.lineBuilder.length() + Term.getPromptLen()) {
            int index = cursorX - Term.getPromptLen();
            if (index == 0 && inChar == CharUtil.SPACE) {
                return false;
            }
            term.lineBuilder.insert(index, inChar);
        } else {
            term.lineBuilder.append(inChar);
        }
        term.executeCursorOldX.getAndUpdate(prev -> {
            if (CharUtil.isChinese(inChar)) {
                return prev + 2;
            } else {
                return ++prev;
            }
        });
        return false;
    }
}
