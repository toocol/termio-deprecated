package com.toocol.termio.core.term.core;

import com.toocol.termio.utilities.event.CharEvent;
import com.toocol.termio.utilities.utils.StrUtil;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:06
 */
public final class ActionCRLF extends TermCharAction {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.CR, CharEvent.LF};
    }

    @Override
    public boolean actOnConsole(Term term, CharEvent charEvent, char inChar) {
        int[] cursorPosition = term.getCursorPosition();
        term.hideCursor();
        term.setCursorPosition(Term.getPromptLen(), cursorPosition[1]);
        term.showCursor();
        term.historyCmdHelper.push(term.lineBuilder.toString());

        term.executeCursorOldX.set(Term.getPromptLen());
        term.printExecution(StrUtil.EMPTY);
        return true;
    }

    @Override
    public boolean actOnDesktop(Term term, CharEvent charEvent, char inChar) {
        return true;
    }
}
