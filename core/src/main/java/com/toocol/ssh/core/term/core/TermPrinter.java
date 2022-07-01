package com.toocol.ssh.core.term.core;

import com.toocol.ssh.utilities.anis.AnisStringBuilder;
import com.toocol.ssh.utilities.utils.StrUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/22 22:03
 * @version: 0.0.1
 */
public record TermPrinter(Term term) {

    public static volatile String DISPLAY_BUFF = StrUtil.EMPTY;
    public static volatile String COMMAND_BUFF = StrUtil.EMPTY;

    synchronized void printExecuteBackground() {
        term.setCursorPosition(Term.LEFT_MARGIN, Term.executeLine);
        AnisStringBuilder builder = new AnisStringBuilder()
                .background(Term.theme.executeBackgroundColor)
                .front(Term.theme.executeFrontColor)
                .append(Term.PROMPT + " ".repeat(term.getWidth() - Term.getPromptLen() - Term.LEFT_MARGIN));
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
                .append(" ".repeat(term.getWidth() - Term.getPromptLen() - Term.LEFT_MARGIN));
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
        while (term.getCursorPosition()[1] < term.getHeight() - 1) {
            Printer.println(" ".repeat(windowWidth));
        }
    }

    synchronized void printDisplayBackground(int lines) {
        term.setCursorPosition(0, Term.executeLine + 1);
        AnisStringBuilder builder = new AnisStringBuilder()
                .background(Term.theme.displayBackGroundColor)
                .append(" ".repeat(term.getWidth() - Term.LEFT_MARGIN - Term.LEFT_MARGIN));
        for (int idx = 0; idx < lines + 2; idx++) {
            Printer.println(builder.toString());
        }
    }

    synchronized void printDisplay(String msg) {
        if (StringUtils.isEmpty(msg)) {
            DISPLAY_BUFF = StrUtil.EMPTY;
            cleanDisplayZone();
            term.setCursorPosition(Term.getPromptLen() + term.lineBuilder.length(), Term.executeLine);
            return;
        }
        DISPLAY_BUFF = msg;
        term.hideCursor();
        cleanDisplayZone();
        int idx = 0;
        String[] split = msg.split("\n");
        printDisplayBackground(split.length);
        for (String line : split) {
            term.setCursorPosition(Term.TEXT_LEFT_MARGIN, Term.executeLine + 2 + idx++);
            Printer.println(new AnisStringBuilder()
                    .background(Term.theme.displayBackGroundColor)
                    .append(line)
                    .toString());
        }
        term.displayZoneBottom = term.getCursorPosition()[1] + 1;
        term.setCursorPosition(Term.getPromptLen() + term.lineBuilder.length(), Term.executeLine);
        term.showCursor();
    }

    synchronized void printDisplayBuffer() {
        if (StringUtils.isEmpty(DISPLAY_BUFF)) {
            cleanDisplayZone();
            term.setCursorPosition(Term.getPromptLen() + term.lineBuilder.length(), Term.executeLine);
            return;
        }
        cleanDisplayZone();
        int idx = 0;
        String[] split = DISPLAY_BUFF.split("\n");
        printDisplayBackground(split.length);
        for (String line : split) {
            term.setCursorPosition(Term.TEXT_LEFT_MARGIN, Term.executeLine + 2 + idx++);
            Printer.println(new AnisStringBuilder()
                    .background(Term.theme.displayBackGroundColor)
                    .append(line)
                    .toString());
        }
        term.displayZoneBottom = term.getCursorPosition()[1] + 1;
        term.setCursorPosition(0, Term.executeLine);
    }

    synchronized void printDisplayEcho(String msg) {
        if (StringUtils.isEmpty(msg)) {
            DISPLAY_BUFF = StrUtil.EMPTY;
            cleanDisplayZone();
            term.setCursorPosition(Term.getPromptLen() + term.lineBuilder.length(), Term.executeLine);
            return;
        }
        DISPLAY_BUFF = msg;
        term.hideCursor();
        cleanDisplayZone();
        int idx = 0;
        String[] split = msg.split("\n");
        printDisplayBackground(split.length);
        for (String line : split) {
            term.setCursorPosition(Term.TEXT_LEFT_MARGIN, Term.executeLine + 2 + idx++);
            Printer.println(new AnisStringBuilder()
                    .background(Term.theme.displayBackGroundColor)
                    .append(line)
                    .toString());
        }
        term.displayZoneBottom = term.getCursorPosition()[1] + 1;
        term.setCursorPosition(term.executeCursorOldX.get(), Term.executeLine);
        term.showCursor();
    }

    synchronized void printCommandBuffer() {
        term.setCursorPosition(Term.getPromptLen(), Term.executeLine);
        Printer.print(new AnisStringBuilder().background(Term.theme.executeBackgroundColor).front(Term.theme.executeFrontColor).append(COMMAND_BUFF).toString());
    }
}
