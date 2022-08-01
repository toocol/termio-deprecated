package com.toocol.termio.core.term.core;

import com.toocol.termio.utilities.event.CharEvent;

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
        int[] cursorPosition = term.getCursorPosition();
        if (cursorPosition[0] < term.lineBuilder.length() + Term.getPromptLen()) {
            term.setCursorPosition(term.lineBuilder.length() + Term.getPromptLen(), cursorPosition[1]);
        }
        return false;
    }
}
