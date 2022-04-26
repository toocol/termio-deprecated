package com.toocol.ssh.core.term.core;

import com.toocol.ssh.utilities.event.CharEvent;
import com.toocol.ssh.utilities.utils.Tuple2;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:02
 */
public final class ActionTab extends TermCharAction {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.TAB};
    }

    @Override
    public boolean act(Term term, CharEvent charEvent, char inChar) {
        Tuple2<Integer, Integer> cursorPosition = term.getCursorPosition();
        if (cursorPosition._1() < term.lineBuilder.length() + Term.getPromptLen()) {
            term.setCursorPosition(term.lineBuilder.length() + Term.getPromptLen(), cursorPosition._2());
        }
        return false;
    }
}
