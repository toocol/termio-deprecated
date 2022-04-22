package com.toocol.ssh.core.term.core;

import com.toocol.ssh.common.utils.StrUtil;
import com.toocol.ssh.core.term.handlers.TerminalEchoHandler;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/22 22:03
 * @version: 0.0.1
 */
public record TermPrinter(Term term) {

    public static volatile String buffer = StrUtil.EMPTY;

    void cleanDisplayZone() {
        term.setCursorPosition(0, Term.executeLine + 1);
        int windowWidth = term.getWidth();
        while (term.getCursorPosition()._2() < term.displayZoneBottom) {
            Printer.println(" ".repeat(windowWidth));
        }
    }

    void printDisplay(String msg) {
        buffer = msg;
        term.hideCursor();
        cleanDisplayZone();
        term.setCursorPosition(0, Term.executeLine + 2);
        Printer.print(msg);
        term.displayZoneBottom = term.getCursorPosition()._2() + 1;
        term.setCursorPosition(Term.PROMPT.length(), Term.executeLine);
        term.showCursor();
    }

    void printDisplayBuffer() {
        cleanDisplayZone();
        term.setCursorPosition(0, Term.executeLine + 2);
        Printer.print(buffer);
        term.displayZoneBottom = term.getCursorPosition()._2() + 1;
        term.setCursorPosition(0, Term.executeLine);
    }

    void printCommandBuffer() {
        term.setCursorPosition(Term.PROMPT.length(), Term.executeLine);
        Printer.print(TerminalEchoHandler.command);
    }

}
