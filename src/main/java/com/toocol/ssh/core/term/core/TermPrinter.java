package com.toocol.ssh.core.term.core;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/22 22:03
 * @version: 0.0.1
 */
public record TermPrinter(Term term) {

    void cleanDisplayZone() {
        term.hideCursor();
        term.setCursorPosition(0, Term.executeLine + 1);
        while (term.getCursorPosition()._2() < term.displayZoneBottom) {
            Printer.print(" ");
        }
    }

    void printDisplay(String msg) {
        cleanDisplayZone();
        term.setCursorPosition(0, Term.executeLine + 2);
        Printer.print(msg);
        term.displayZoneBottom = term.getCursorPosition()._2() + 1;
        term.setCursorPosition(Term.PROMPT.length(), Term.executeLine);
        term.showCursor();
    }

}
