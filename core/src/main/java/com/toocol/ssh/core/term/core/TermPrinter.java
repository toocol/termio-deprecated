package com.toocol.ssh.core.term.core;

import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.utilities.anis.AnisStringBuilder;
import com.toocol.ssh.utilities.anis.Printer;
import com.toocol.ssh.utilities.utils.PomUtil;
import com.toocol.ssh.utilities.utils.StrUtil;
import org.apache.commons.lang3.StringUtils;

import static com.toocol.ssh.core.term.core.Term.CONSOLE;

/**
 * The print area of the term screen, from top to bottom is:
 * 1. information bar
 * 2. scene
 * 3. execution
 * 4. display
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/22 22:03
 * @version: 0.0.1
 */
public record TermPrinter(Term term) {
    private static final CredentialCache credentialCache = CredentialCache.getInstance();
    private static final Runtime RUNTIME = Runtime.getRuntime();

    public static volatile String DISPLAY_BUFF = StrUtil.EMPTY;
    public static volatile String COMMAND_BUFF = StrUtil.EMPTY;

    private static long totalMemory() {
        return RUNTIME.totalMemory() / 1024 / 1024;
    }

    private static long maxMemory() {
        return RUNTIME.maxMemory() / 1024 / 1024;
    }

    private static long freeMemory() {
        return RUNTIME.freeMemory() / 1024 / 1024;
    }

    private static long usedMemory() {
        return totalMemory() - freeMemory();
    }

    private void printInformationBar() {
        int windowWidth = CONSOLE.getWindowWidth();

        CONSOLE.setCursorPosition(0, 0);

        String termioVersion = " termio: V" + PomUtil.getVersion();
        String memoryUse = "memory-use: " + usedMemory() + "MB";
        String active = "alive: " + SshSessionCache.getAlive();
        String website = "https://github.com/Joezeo/termio ";
        int totalLen = termioVersion.length() + website.length() + memoryUse.length() + active.length();
        if (totalLen >= windowWidth) {
            return;
        }
        String space = " ".repeat((windowWidth - totalLen) / 3);

        String merge = termioVersion + space + memoryUse + space + active + space + website;
        int fulfil = windowWidth - merge.length();
        if (fulfil != 0) {
            merge = merge.replaceAll(website, " ".repeat(fulfil)) + website;
        }

        AnisStringBuilder builder = new AnisStringBuilder()
                .background(Term.theme.infoBarBackgroundColor)
                .front(Term.theme.infoBarFrontColor)
                .append(merge);
        Printer.println(builder.toString());
        Printer.println();
    }


    public void printScene(boolean resize) {
        Term term = Term.getInstance();
        int[] oldPosition = term.getCursorPosition();
        CONSOLE.hideCursor();
        if (resize) {
            Printer.clear();
        }
        printInformationBar();
        Printer.print("Properties:                                                                           \n");
        if (credentialCache.credentialsSize() == 0) {
            Printer.print("You have no connection properties, type 'help' to get more information.                         \n\n");
        } else {
            credentialCache.showCredentials();
        }

        for (int idx = 0; idx < Term.TOP_MARGIN; idx++) {
            Printer.println();
        }

        Term.executeLine = term.getCursorPosition()[1];
        term.printExecuteBackground();
        if (resize && oldPosition[0] != 0 && oldPosition[1] != 0) {
            term.printDisplayBuffer();
            printTermPrompt();
            term.printCommandBuffer();
        }
        if (!resize) {
            term.setCursorPosition(Term.getPromptLen(), Term.executeLine);
        }
        CONSOLE.showCursor();
    }

    public void printTermPrompt() {
        term.printExecuteBackground();
        term.setCursorPosition(Term.getPromptLen(), Term.executeLine);
    }

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

    synchronized void cleanDisplay() {
        term.setCursorPosition(0, Term.executeLine + 1);
        int windowWidth = term.getWidth();
        while (term.getCursorPosition()[1] < term.getHeight() - 2) {
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
            cleanDisplay();
            term.setCursorPosition(Term.getPromptLen() + term.lineBuilder.length(), Term.executeLine);
            return;
        }
        DISPLAY_BUFF = msg;
        term.hideCursor();
        cleanDisplay();
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
            cleanDisplay();
            term.setCursorPosition(Term.getPromptLen() + term.lineBuilder.length(), Term.executeLine);
            return;
        }
        cleanDisplay();
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
            cleanDisplay();
            term.setCursorPosition(Term.getPromptLen() + term.lineBuilder.length(), Term.executeLine);
            return;
        }
        DISPLAY_BUFF = msg;
        term.hideCursor();
        cleanDisplay();
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

    synchronized void printTest() {
        cleanDisplay();
    }
}
