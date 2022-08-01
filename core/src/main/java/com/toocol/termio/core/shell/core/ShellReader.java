package com.toocol.termio.core.shell.core;

import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.core.term.core.TermStatus;
import com.toocol.termio.utilities.utils.CharUtil;
import jline.console.ConsoleReader;
import sun.misc.Signal;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/13 2:07
 * @version: 0.0.1
 */
record ShellReader(Shell shell, ConsoleReader reader) {

    void initReader() {
        /*
         * custom handle CTRL+C
         */
        Signal.handle(new Signal("INT"), signal -> {
            if (Term.status.equals(TermStatus.TERMIO)) {
                return;
            }
            if (shell.status.equals(Shell.Status.QUICK_SWITCH)) {
                return;
            }
            try {
                shell.historyCmdHelper.reset();
                shell.localLastCmd.delete(0, shell.localLastCmd.length());
                shell.cmd.delete(0, shell.cmd.length());
                shell.writeAndFlush(CharUtil.CTRL_C);
                shell.status = Shell.Status.NORMAL;
            } catch (Exception e) {
                // do nothing
            }
        });
    }

    void readCmd() throws Exception {
        shell.cmd.delete(0, shell.cmd.length());
        while (true) {
            char inChar = (char) reader.readCharacter();

            /*
             * Start to deal with arrow key.
             */
            char finalChar;

            if (shell.status.equals(Shell.Status.QUICK_SWITCH)) {
                finalChar = shell.escapeHelper.processArrowBundle(inChar, shell, reader);
            } else {
                finalChar = shell.escapeHelper.processArrowStream(inChar);
            }

            if (shell.status.equals(Shell.Status.VIM_UNDER)) {

                char vimChar = shell.escapeHelper.processArrowBundle(finalChar, shell, reader);

                shell.writeAndFlush(shell.vimHelper.transferVimInput(vimChar));

            } else if (shell.status.equals(Shell.Status.MORE_PROC)
                    || shell.status.equals(Shell.Status.MORE_EDIT)
                    || shell.status.equals(Shell.Status.MORE_SUB)) {

                boolean support;
                switch (shell.status) {
                    case MORE_PROC -> support = shell.moreHelper.support(finalChar);
                    case MORE_SUB -> support = shell.moreHelper.supportSub(finalChar);
                    case MORE_EDIT -> support = shell.moreHelper.supportEdit(finalChar);
                    default -> support = false;
                }
                if (support) {
                    shell.writeAndFlush(finalChar);
                }

            } else {
                if (shell.shellCharEventDispatcher.dispatch(shell, finalChar)) {
                    break;
                }
            }
        }
    }
}