package com.toocol.termio.core.term.core;

import com.toocol.termio.core.Termio;
import com.toocol.termio.utilities.action.AbstractCharAction;
import com.toocol.termio.utilities.event.CharEvent;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:13
 */
public abstract class TermCharAction extends AbstractCharAction<Term> {

    @Override
    public boolean act(Term term, CharEvent charEvent, char inChar) {
        return Termio.runType().equals(Termio.RunType.CONSOLE) ?
                actOnConsole(term, charEvent, inChar) : actOnDesktop(term, charEvent, inChar);
    }

    public abstract boolean actOnConsole(Term term, CharEvent charEvent, char inChar);

    public abstract boolean actOnDesktop(Term term, CharEvent charEvent, char inChar);
}
