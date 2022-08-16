package com.toocol.termio.core.term.core;

import com.toocol.termio.core.cache.CredentialCache;
import com.toocol.termio.core.cache.SshSessionCache;
import com.toocol.termio.utilities.ansi.AnsiStringBuilder;
import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.utils.PomUtil;
import com.toocol.termio.utilities.utils.StrUtil;
import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;

import static com.toocol.termio.core.term.core.Term.CONSOLE;

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

        AnsiStringBuilder builder = new AnsiStringBuilder()
                .background(Term.theme.infoBarBackgroundColor.color)
                .front(Term.theme.infoBarFrontColor.color)
                .append(merge);
        Printer.println(builder.toString());
    }

    synchronized void printScene(boolean resize) {
        Term term = Term.getInstance();
        int[] oldPosition = term.getCursorPosition();
        CONSOLE.hideCursor();
        if (resize) {
            Printer.clear();
        }
        printInformationBar();
        String prompt = "Properties:";
        AnsiStringBuilder builder = new AnsiStringBuilder();
        int width = term.getWidth();
        Printer.println(
                builder.background(Term.theme.propertiesZoneBgColor.color)
                        .space(width).crlf()
                        .append(prompt).space(width - prompt.length())
                        .toString()
        );
        builder.clearStr();
        if (credentialCache.credentialsSize() == 0) {
            String msg = "You have no connection properties, type 'help' to get more information.";
            Printer.print(
                    builder.append(msg)
                            .space(width - msg.length()).crlf()
                            .space(width).crlf()
                            .toString()
            );
        } else {
            credentialCache.showCredentials();
        }

        for (int idx = 0; idx < Term.TOP_MARGIN; idx++) {
            Printer.println(builder.space(width).toString());
        }

        String msg = "'←'/'→' to change groups.";
        builder.clearColor().clearStr();
        String[] groups = new String[] {"default", "group_1", "group_2", "group_3"};
        int groupsLen = Arrays.stream(groups).mapToInt(String::length).sum();
        builder.background(Term.theme.groupActiveBgColor.color).space(5).append(groups[0]).space(5)
                .background(Term.theme.groupSplitBgColor.color).space()
                .background(Term.theme.groupIdleBgColor.color)
                .space(5).append(groups[1]).space(5).background(Term.theme.groupSplitBgColor.color).space()
                .background(Term.theme.groupIdleBgColor.color)
                .space(5).append(groups[2]).space(5).background(Term.theme.groupSplitBgColor.color).space()
                .background(Term.theme.groupIdleBgColor.color)
                .space(5).append(groups[3]).space(5)
                .deBackground()
                .space(width - (8 * 5 + 3) - groupsLen - msg.length())
                .append(msg);
        Printer.println(builder.toString());

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

    synchronized void printTermPrompt() {
        term.printExecuteBackground();
        term.setCursorPosition(Term.getPromptLen(), Term.executeLine);
    }

    synchronized void printExecuteBackground() {
        term.setCursorPosition(Term.LEFT_MARGIN, Term.executeLine);
        AnsiStringBuilder builder = new AnsiStringBuilder()
                .background(Term.theme.executeBackgroundColor.color)
                .front(Term.theme.executeFrontColor.color)
                .append(Term.PROMPT + " ".repeat(term.getWidth() - Term.getPromptLen() - Term.LEFT_MARGIN));
        Printer.print(builder.toString());
        term.showCursor();
    }

    synchronized void printExecution(String msg) {
        COMMAND_BUFF = msg;
        term.hideCursor();
        term.setCursorPosition(Term.getPromptLen(), Term.executeLine);

        AnsiStringBuilder builder = new AnsiStringBuilder()
                .background(Term.theme.executeBackgroundColor.color)
                .front(Term.theme.executeFrontColor.color)
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
        AnsiStringBuilder builder = new AnsiStringBuilder()
                .background(Term.theme.displayBackGroundColor.color)
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
            Printer.println(new AnsiStringBuilder()
                    .background(Term.theme.displayBackGroundColor.color)
                    .append(line)
                    .toString());
        }
        term.displayZoneBottom = term.getCursorPosition()[1] + 1;
        term.setCursorPosition(Term.getPromptLen() + term.lineBuilder.length(), Term.executeLine);
        term.showCursor();
    }

    synchronized void printDisplay(String msg,Boolean judgment) {
        if (judgment) {
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
                Printer.println(new AnsiStringBuilder()
                        .background(Term.theme.displayBackGroundColor.color)
                        .append(line)
                        .toString());
            }
            term.displayZoneBottom = term.getCursorPosition()[1] + 1;
            term.setCursorPosition(Term.getPromptLen() + term.lineBuilder.length(), Term.executeLine);
            term.showCursor();
        }
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
            Printer.println(new AnsiStringBuilder()
                    .background(Term.theme.displayBackGroundColor.color)
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
            Printer.println(new AnsiStringBuilder()
                    .background(Term.theme.displayBackGroundColor.color)
                    .append(line)
                    .toString());
        }
        term.displayZoneBottom = term.getCursorPosition()[1] + 1;
        term.setCursorPosition(term.executeCursorOldX.get(), Term.executeLine);
        term.showCursor();
    }

    synchronized void printCommandBuffer() {
        term.setCursorPosition(Term.getPromptLen(), Term.executeLine);
        Printer.print(new AnsiStringBuilder().background(Term.theme.executeBackgroundColor.color).front(Term.theme.executeFrontColor.color).append(COMMAND_BUFF).toString());
    }

    synchronized void printTest() {
        Printer.clear();
//        Printer.print("aaaaaaaaaaaaaaaaaaa");
//        Printer.print("\u001b[Hbbbbbbbbbbbbbbbbbbb");
//        Printer.println();
//        Printer.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        Printer.println("aaaaaaaaaaaaaaaaaaa");
        Printer.print("bbb");
        Printer.print("\u001b[2;0Hc");
//        String msg = "\u001B[0m\u001B[1;49r\u001B[49;1H\n" +
//                "\u001B[r\u001B[48;1H[root@vultrguest /]# ls\n" +
//                "\u001B[0;1;36mbin\u001B[0m  \u001B[0;1;34mboot\u001B[0m  \u001B[0;1;34mdata\u001B[0m  \u001B[0;1;34mdev\u001B[0m  \u001B[0;1;34metc\u001B[0m  \u001B[0;1;34mhome\u001B[0m  \u001B[0;1;36mlib\u001B[0m  \u001B[0;1;36mlib64\u001B[0m  \u001B[0;1;34mlost+found\u001B[0m  \u001B[0;1;34mmedia\u001B[0m  \u001B[0;1;34mmnt\u001B[0m  \u001B[0;1;34mopt\u001B[0m  \u001B[0;1;34mproc\u001B[0m  \u001B[0;1;34mroot\u001B[0m  \u001B[0;1;34mrun\u001B[0m  \u001B[0;1;36msbin\u001B[0m  \u001B[0;1;34msrv\u001B[0m  \u001B[0;1;34msys\u001B[0m  \u001B[0;30;42mtmp\u001B[0m  \u001B[0;1;34musr\u001B[0m  \u001B[0;1;34mvar\u001B[50;22H\u001B[0m";
//        Printer.print(msg);
//        CONSOLE.rollingProcessing(msg);

//        msg = "\u001B[0m\u001B[1;49r\u001B[49;1H\n" +
//                "\u001B[r\u001B[25;1H[root@vultrguest /]# ll -a\n" +
//                "total 80\n" +
//                "dr-xr-xr-x.  19 root root  4096 Jul  2 21:35 \u001B[0;1;34m.\n" +
//                "\u001B[0mdr-xr-xr-x.  19 root root  4096 Jul  2 21:35 \u001B[0;1;34m..\n" +
//                "\u001B[0mlrwxrwxrwx.   1 root root     7 Jun 22  2021 \u001B[0;1;36mbin\u001B[0m -> \u001B[0;1;34musr/bin\n" +
//                "\u001B[0mdr-xr-xr-x.   5 root root  4096 Jun 18 16:54 \u001B[0;1;34mboot\n" +
//                "\u001B[0mdrwxr-xr-x.   3 root root  4096 Jan 24  2021 \u001B[0;1;34mdata\n" +
//                "\u001B[0mdrwxr-xr-x.  19 root root  2920 Jun 22  2021 \u001B[0;1;34mdev\n" +
//                "\u001B[0mdrwxr-xr-x. 107 root root 12288 Jul 10 23:46 \u001B[0;1;34metc\n" +
//                "\u001B[0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 \u001B[0;1;34mhome\n" +
//                "\u001B[0mlrwxrwxrwx.   1 root root     7 Jun 22  2021 \u001B[0;1;36mlib\u001B[0m -> \u001B[0;1;34musr/lib\n" +
//                "\u001B[0mlrwxrwxrwx.   1 root root     9 Jun 22  2021 \u001B[0;1;36mlib64\u001B[0m -> \u001B[0;1;34musr/lib64\n" +
//                "\u001B[0mdrwx------.   2 root root 16384 Feb 13  2020 \u001B[0;1;34mlost+found\n" +
//                "\u001B[0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 \u001B[0;1;34mmedia\n" +
//                "\u001B[0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 \u001B[0;1;34mmnt\n" +
//                "\u001B[0mdrwxr-xr-x.   4 root root  4096 Jun 22  2021 \u001B[0;1;34mopt\n" +
//                "\u001B[0mdr-xr-xr-x. 101 root root     0 Apr  6 10:19 \u001B[0;1;34mproc\n" +
//                "\u001B[0mdr-xr-x---.  10 root root  4096 Jul 25 20:01 \u001B[0;1;34mroot\n" +
//                "\u001B[0mdrwxr-xr-x.  35 root root  1000 Jul 24 03:06 \u001B[0;1;31;45,0mrun\n" +
//                "\u001B[0mlrwxrwxrwx.   1 root root     8 Jun 22  2021 \u001B[0;1;36msbin\u001B[0m -> \u001B[0;1;34musr/sbin\n" +
//                "\u001B[0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 \u001B[0;1;34msrv\n" +
//                "\u001B[0mdr-xr-xr-x.  13 root root     0 Apr  6 10:19 \u001B[0;1;34msys\n" +
//                "\u001B[0mdrwxrwxrwt.   4 root root  4096 Jul 25 22:26 \u001B[0;30;42mtmp\n" +
//                "\u001B[0mdrwxr-xr-x.  13 root root  4096 Jun 18 16:48 \u001B[0;1;34musr\n" +
//                "\u001B[0mdrwxr-xr-x.  21 root root  4096 Jun 18 16:42 \u001B[0;1;34mvar\u001B[50;22H\u001B[0m";
//        Printer.print(msg);
    }

    public void printColorPanel() {

    }
}
