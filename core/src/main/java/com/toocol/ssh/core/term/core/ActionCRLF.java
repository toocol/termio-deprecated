package com.toocol.ssh.core.term.core;

import com.toocol.ssh.utilities.event.CharEvent;
import com.toocol.ssh.utilities.utils.StrUtil;
import com.toocol.ssh.utilities.utils.Tuple2;

import static com.toocol.ssh.core.term.TermAddress.TERMINAL_ECHO;

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
    public boolean act(Term term, CharEvent charEvent, char inChar) {
        Tuple2<Integer, Integer> cursorPosition = term.getCursorPosition();
        term.hideCursor();
        term.setCursorPosition(Term.PROMPT.length(), cursorPosition._2());
        term.setCursorPosition(Term.PROMPT.length(), cursorPosition._2());
        term.showCursor();
        term.historyHelper.push(term.lineBuilder.toString());

        term.executeCursorOldX.set(Term.PROMPT.length());
        term.printExecution(StrUtil.EMPTY);
        term.eventBus.send(TERMINAL_ECHO.address(), StrUtil.EMPTY);
        return true;
    }
}
