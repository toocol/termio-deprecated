package com.toocol.ssh.core.term.core;

import com.toocol.ssh.utilities.anis.AnisStringBuilder;
import com.toocol.ssh.utilities.anis.ColorHelper;
import com.toocol.ssh.utilities.utils.StrUtil;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/leftMargin/22 22:03
 * @version: 0.0.1
 */
public record TermPrinter(Term term) {

    public static volatile String DISPLAY_BUFF = StrUtil.EMPTY;
    public static volatile String COMMAND_BUFF = StrUtil.EMPTY;
    
    public static final int LEFT_MARGIN = 4;
    public static final int TEXT_LEFT_MARGIN = 5;

    synchronized void printExecuteBackground() {
        String transition = ColorHelper.background(" ".repeat(term.getWidth() - 8), Term.theme.transitionBackground);
        term.hideCursor();
        term.setCursorPosition(LEFT_MARGIN, Term.executeLine - 1);
        Printer.println(transition);

        term.setCursorPosition(4, Term.executeLine);
        AnisStringBuilder builder = new AnisStringBuilder()
                .background(Term.theme.executeBackgroundColor)
                .front(Term.theme.executeFrontColor)
                .append(Term.PROMPT + " ".repeat(term.getWidth() - Term.getPromptLen() - 4));
        Printer.print(builder.toString());
        term.showCursor();
    }

    synchronized void printExecution(String msg) {
        COMMAND_BUFF = msg;
        term.hideCursor();
        term.setCursorPosition(Term.getPromptLen(), Term.executeLine);

        AnisStringBuilder builder = new AnisStringBuilder()
                .background(Term.theme.executeBackgroundColor)
                .front(Term.theme.executeFrontColor)
                .append(" ".repeat(term.getWidth() - Term.getPromptLen() - 4));
        Printer.print(builder.toString());
        term.setCursorPosition(Term.getPromptLen(), Term.executeLine);

        builder.clearStr().append(msg);
        Printer.print(builder.toString());
        term.setCursorPosition(term.executeCursorOldX.get(), Term.executeLine);
        term.showCursor();
    }

    synchronized void cleanDisplayZone() {
        term.setCursorPosition(0, Term.executeLine + 1);
        int windowWidth = term.getWidth();
        while (term.getCursorPosition()._2() < term.getHeight() - 1) {
            Printer.println(" ".repeat(windowWidth));
        }
    }

    synchronized void printDisplayBackground() {
        int width = term.getWidth();
        int height = term.getHeight() - Term.executeLine - 5;

        String transition = ColorHelper.background(" ".repeat(width - 8), Term.theme.transitionBackground);
        String backLine = ColorHelper.background(" ".repeat(width - 8), Term.theme.displayBackGroundColor);
        term.hideCursor();
        term.setCursorPosition(LEFT_MARGIN, Term.executeLine + 1);
        Printer.println(transition);
        for (int idx = 0; idx < height; idx ++) {
            term.setCursorPosition(LEFT_MARGIN, Term.executeLine + 2 + idx);
            Printer.println(backLine);
        }
        term.setCursorPosition(LEFT_MARGIN, Term.executeLine + 2);
        term.showCursor();
    }

    synchronized void printDisplay(String msg) {
        DISPLAY_BUFF = msg;
        term.hideCursor();
        cleanDisplayZone();
        printDisplayBackground();
        int idx = 0;
        for (String line : msg.split("\n")) {
            term.setCursorPosition(TEXT_LEFT_MARGIN, Term.executeLine + 3 + idx++);
            Printer.println(new AnisStringBuilder().background(Term.theme.displayBackGroundColor).append(line).toString());
        }
        term.displayZoneBottom = term.getCursorPosition()._2() + 1;
        term.setCursorPosition(Term.getPromptLen() + term.lineBuilder.length(), Term.executeLine);
        term.showCursor();
    }

    synchronized void printDisplayBuffer() {
        cleanDisplayZone();
        printDisplayBackground();
        term.setCursorPosition(TEXT_LEFT_MARGIN, Term.executeLine + 3);
        int idx = 0;
        for (String line : DISPLAY_BUFF.split("\n")) {
            term.setCursorPosition(TEXT_LEFT_MARGIN, Term.executeLine + 3 + idx++);
            Printer.print(new AnisStringBuilder().background(Term.theme.displayBackGroundColor).append(line).toString());
        }
        term.displayZoneBottom = term.getCursorPosition()._2() + 1;
        term.setCursorPosition(0, Term.executeLine);
    }

    synchronized void printDisplayEcho(String msg) {
        DISPLAY_BUFF = msg;
        term.hideCursor();
        cleanDisplayZone();
        printDisplayBackground();
        int idx = 0;
        for (String line : msg.split("\n")) {
            term.setCursorPosition(TEXT_LEFT_MARGIN, Term.executeLine + 3 + idx++);
            Printer.println(new AnisStringBuilder().background(Term.theme.displayBackGroundColor).append(line).toString());
        }
        term.displayZoneBottom = term.getCursorPosition()._2() + 1;
        term.setCursorPosition(term.executeCursorOldX.get(), Term.executeLine);
        term.showCursor();
    }

    synchronized void printCommandBuffer() {
        term.setCursorPosition(Term.getPromptLen(), Term.executeLine);
        Printer.print(new AnisStringBuilder().background(Term.theme.executeBackgroundColor).front(Term.theme.executeFrontColor).append(COMMAND_BUFF).toString());
    }
}
