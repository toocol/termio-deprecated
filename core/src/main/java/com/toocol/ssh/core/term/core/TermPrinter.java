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
        while (term.getCursorPosition()[1] < term.getHeight() - 3) {
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
        Printer.clear();
        String msg = "[0m\u001B[1;46r\u001B[46;1H\n" +
                "\u001B[r\u001B[45;1H[root@vultrguest /]# ls\n" +
                "\u001B[0;1;36mbin\u001B[0m  \u001B[0;1;34mboot\u001B[0m  \u001B[0;1;34mdata\u001B[0m  \u001B[0;1;34mdev\u001B[0m  \u001B[0;1;34metc\u001B[0m  \u001B[0;1;34mhome\u001B[0m  \u001B[0;1;36mlib\u001B[0m  \u001B[0;1;36mlib64\u001B[0m  \u001B[0;1;34mlost+found\u001B[0m  \u001B[0;1;34mmedia\u001B[0m  \u001B[0;1;34mmnt\u001B[0m  \u001B[0;1;34mopt\u001B[0m  \u001B[0;1;34mproc\u001B[0m  \u001B[0;1;34mroot\u001B[0m  \u001B[0;1;34mrun\u001B[0m  \u001B[0;1;36msbin\u001B[0m  \u001B[0;1;34msrv\u001B[0m  \u001B[0;1;34msys\u001B[0m  \u001B[0;30;42mtmp\u001B[0m  \u001B[0;1;34musr\u001B[0m  \u001B[0;1;34mvar\u001B[50;22H\u001B[0m";
        Printer.print(msg);
        CONSOLE.rollingProcessing(msg);

        msg = "\u001B[0m\u001B[1;46r\u001B[46;1H\n" +
                "\u001B[r\u001B[30;1Hroot     2402148       1  0 00:02 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2402375       1  0 00:03 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2402561       1  0 00:08 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2402865       1  0 00:22 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2402986       1  0 00:23 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403088       1  0 00:23 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403197       1  0 00:24 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403275       1  0 00:25 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403403       1  0 00:27 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403471       1  0 00:29 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403521       1  0 00:30 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403579       1  0 00:31 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403651       1  0 00:33 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403734       1  0 00:34 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403815       1  0 00:36 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403890       1  0 00:37 pts/0    00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2404001 2403891  0 00:39 pts/16   00:00:00 grep --color=auto \u001B[0;1;31mmosh\n" +
                "\u001B[0m[root@vultrguest v2ray]# ";
        Printer.print(msg);
        CONSOLE.rollingProcessing(msg);
        Printer.print("\u001B[0m\u001B[1;46r\u001B[46;1H\n" +
                "\u001B[r\u001B[30;1Hroot     2402148       1  0 00:02 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2402375       1  0 00:03 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2402561       1  0 00:08 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2402865       1  0 00:22 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2402986       1  0 00:23 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403088       1  0 00:23 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403197       1  0 00:24 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403275       1  0 00:25 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403403       1  0 00:27 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403471       1  0 00:29 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403521       1  0 00:30 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403579       1  0 00:31 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403651       1  0 00:33 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403734       1  0 00:34 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403815       1  0 00:36 ?        00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2403890       1  0 00:37 pts/0    00:00:00 \u001B[0;1;31mmosh\u001B[0m-server\n" +
                "root     2404060 2403891  0 00:40 pts/16   00:00:00 grep --color=auto \u001B[0;1;31mmosh\n" +
                "\u001B[0m[root@vultrguest v2ray]# ");
    }
}
