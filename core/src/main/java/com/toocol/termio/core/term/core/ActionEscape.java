package com.toocol.termio.core.term.core;

import com.toocol.termio.utilities.event.CharEvent;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:06
 */
public final class ActionEscape extends TermCharAction {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.ESCAPE};
    }

    @Override
    public boolean act(Term device, CharEvent charEvent, char inChar) {
        if (TermStatus.HISTORY_OUTPUT.equals(Term.status)) {
            device.cleanDisplay();
            Term.status = TermStatus.TERMIO;
        }
        return false;
    }
}
