package com.toocol.ssh.core.term.core;

import static com.toocol.ssh.core.term.TermAddress.TERMINAL_ECHO;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/16 15:23
 */
public record TermReader(Term term) {

    String readLine() {
        term.lineBuilder.delete(0, term.lineBuilder.length());
        term.executeCursorOldX.set(term.getCursorPosition()._1());
        try {
            while (true) {
                char inChar = (char) term.reader.readCharacter();
                char finalChar = term.escapeHelper.processArrowStream(inChar);

                if (term.termCharEventDispatcher.dispatch(term, finalChar)) {
                    return term.lineBuilder.toString();
                }

                term.printExecution(term.lineBuilder.toString());
                term.eventBus.send(TERMINAL_ECHO.address(), term.lineBuilder.toString());
            }

        } catch (Exception e) {
            Printer.println("\nSomething error.");
            System.exit(-1);
        }
        return null;
    }
}
