package com.toocol.ssh.core.term.core;

import com.toocol.ssh.utilities.anis.Printer;
import com.toocol.ssh.utilities.event.CharEvent;
import com.toocol.ssh.utilities.utils.StrUtil;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:04
 */
public final class ActionCtrlK extends TermCharAction {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.CTRL_K};
    }

    @Override
    public boolean act(Term term, CharEvent charEvent, char inChar) {
        Printer.clear();
        term.lineBuilder.delete(0, term.lineBuilder.length());
        term.executeCursorOldX.set(Term.getPromptLen());
        term.printScene(false);
        TermPrinter.DISPLAY_BUFF = StrUtil.EMPTY;
        return false;
    }
}
