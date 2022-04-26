package com.toocol.ssh.core.term.core;

import com.toocol.ssh.utilities.anis.AnisStringBuilder;
import com.toocol.ssh.utilities.anis.HighlightHelper;
import com.toocol.ssh.utilities.utils.StrUtil;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/leftMargin/22 22:03
 * @version: 0.0.1
 */
public record TermPrinter(Term term) {

    public static volatile String displayBuffer = StrUtil.EMPTY;
    public static volatile String commandBuffer = StrUtil.EMPTY;
    
    public static final int leftMargin = 4;
    public static final int textLeftMargin = 5;

    synchronized void cleanDisplayZone() {
        term.setCursorPosition(0, Term.executeLine + 1);
        int windowWidth = term.getWidth();
        while (term.getCursorPosition()._2() < term.getHeight() - 1) {
            Printer.println(" ".repeat(windowWidth));
        }
    }

    synchronized void printExecuteBackground() {
        term.hideCursor();
        term.setCursorPosition(4, Term.executeLine);
        Printer.print(HighlightHelper.assembleColorBackground(Term.PROMPT + " ".repeat(term.getWidth() - Term.PROMPT.length() - 8), Term.theme.backgroundColor));
        term.showCursor();
    }

    synchronized void printBackground() {
        int width = term.getWidth();
        int height = term.getHeight() - Term.executeLine - 5;

        String backLine = HighlightHelper.assembleColorBackground(" ".repeat(width - 8), Term.theme.backgroundColor);
        String transition = HighlightHelper.assembleColorBackground(" ".repeat(width - 8), Term.theme.transitionBackground);
        term.hideCursor();
        term.setCursorPosition(leftMargin, Term.executeLine + 1);
        Printer.println(transition);
        for (int idx = 0; idx < height; idx ++) {
            term.setCursorPosition(leftMargin, Term.executeLine + 2 + idx);
            Printer.println(backLine);
        }
        term.setCursorPosition(leftMargin, Term.executeLine + 2);
        term.showCursor();
    }

    synchronized void printDisplay(String msg) {
        displayBuffer = msg;
        term.hideCursor();
        cleanDisplayZone();
        printBackground();
        int idx = 0;
        for (String line : msg.split("\n")) {
            term.setCursorPosition(textLeftMargin, Term.executeLine + 3 + idx++);
            Printer.println(new AnisStringBuilder().background(Term.theme.backgroundColor).append(line).toString());
        }
        term.displayZoneBottom = term.getCursorPosition()._2() + 1;
        term.setCursorPosition(Term.PROMPT.length() + 4 + term.lineBuilder.length(), Term.executeLine);
        term.showCursor();
    }

    synchronized void printDisplayBuffer() {
        cleanDisplayZone();
        printBackground();
        term.setCursorPosition(textLeftMargin, Term.executeLine + 3);
        Printer.print(new AnisStringBuilder().background(Term.theme.backgroundColor).append(displayBuffer).toString());
        term.displayZoneBottom = term.getCursorPosition()._2() + 1;
        term.setCursorPosition(0, Term.executeLine);
    }

    synchronized void printCommandBuffer() {
        term.setCursorPosition(Term.PROMPT.length() + 4, Term.executeLine);
        Printer.print(new AnisStringBuilder().background(Term.theme.backgroundColor).append(commandBuffer).toString());
    }

    synchronized void printDisplayEcho(String msg) {
        displayBuffer = msg;
        term.hideCursor();
        cleanDisplayZone();
        printBackground();
        int idx = 0;
        for (String line : msg.split("\n")) {
            term.setCursorPosition(textLeftMargin, Term.executeLine + 3 + idx++);
            Printer.println(new AnisStringBuilder().background(Term.theme.backgroundColor).append(line).toString());
        }
        term.displayZoneBottom = term.getCursorPosition()._2() + 1;
        term.setCursorPosition(term.executeCursorOldX.get(), Term.executeLine);
        term.showCursor();
    }

    synchronized void printExecution(String msg) {
        commandBuffer = msg;
        term.hideCursor();
        term.setCursorPosition(Term.PROMPT.length() + 4, Term.executeLine);
        Printer.print(HighlightHelper.assembleColorBackground(" ".repeat(term.getWidth() - Term.PROMPT.length() - 8), Term.theme.backgroundColor));
        term.setCursorPosition(Term.PROMPT.length() + 4, Term.executeLine);
        Printer.print(HighlightHelper.assembleColorBackground(msg, Term.theme.backgroundColor));
        term.setCursorPosition(term.executeCursorOldX.get(), Term.executeLine);
        term.showCursor();
    }
}
