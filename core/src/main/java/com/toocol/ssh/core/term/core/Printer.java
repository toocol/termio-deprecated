package com.toocol.ssh.core.term.core;

import com.toocol.ssh.utilities.anis.AnisStringBuilder;
import com.toocol.ssh.utilities.anis.ColorHelper;
import com.toocol.ssh.utilities.console.Console;
import com.toocol.ssh.utilities.utils.ExitMessage;
import com.toocol.ssh.utilities.utils.PomUtil;
import com.toocol.ssh.utilities.utils.Tuple2;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.cache.StatusCache;

import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;

import static com.toocol.ssh.core.config.SystemConfig.*;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:20
 */
public final class Printer {
    public static final PrintStream PRINTER = System.out;

    private static final Runtime RUNTIME = Runtime.getRuntime();

    private static final Console CONSOLE = Console.get();

    private static final String[] patterns = new String[]{"-",
            "\\",
            "|",
            "/",
            "-"};

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

    public static void print(String msg) {
        PRINTER.print(msg);
    }

    public static void println() {
        PRINTER.println();
    }

    public static void println(String msg) {
        PRINTER.println(msg);
    }

    public static void voice() {
        PRINTER.print("\u0007");
    }

    public static void printErr(String msg) {
        println(ColorHelper.front(msg, 167));
    }

    public static void virtualBackspace() {
        print("\b");
        print(" ");
        print("\b");
    }

    public static void printTermPrompt() {
        Term term = Term.getInstance();
        term.printExecuteBackground();
        term.setCursorPosition(Term.getPromptLen(), Term.executeLine);
    }

    public static void printScene(boolean resize) {
        Term term = Term.getInstance();
        Tuple2<Integer, Integer> oldPosition = term.getCursorPosition();
        CONSOLE.hideCursor();
        if (resize) {
            clear();
        }
        printInformationBar();
        print("Properties:                                                                           \n");
        if (CredentialCache.credentialsSize() == 0) {
            print("You have no connection properties, type 'help' to get more information.                         \n\n");
        } else {
            CredentialCache.showCredentials();
        }

        for (int idx = 0; idx < Term.TOP_MARGIN; idx++) {
            println();
        }

        Term.executeLine = term.getCursorPosition()._2();
        term.printExecuteBackground();
        if (resize && oldPosition._1() != 0 && oldPosition._2() != 0) {
            term.printDisplayBuffer();
            printTermPrompt();
            term.printCommandBuffer();
        }
        CONSOLE.showCursor();
    }

    private static void printInformationBar() {
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
                .background(Term.theme.infoBarBackground)
                .front(Term.theme.infoBarFront)
                .append(merge);
        println(builder.toString());
        println();
    }

    @SuppressWarnings("all")
    public static void printLoading(CountDownLatch latch) {
        CONSOLE.hideCursor();
        clear();
        new Thread(() -> {
            int idx = 0;
            print(patterns[idx++] + " starting termio.");
            try {
                while (true) {
                    if (StatusCache.LOADING_ACCOMPLISH) {
                        break;
                    }
                    CONSOLE.setCursorPosition(0, 0);
                    ;
                    print(patterns[idx++]);
                    if (idx >= patterns.length) {
                        idx = 1;
                    }
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                ExitMessage.setMsg("Start up failed.");
                System.exit(-1);
            }
            latch.countDown();
            CONSOLE.showCursor();
        }).start();
    }

    public static void clear() {
        getExecuteMode().ifPresent(executeMode -> getClearCmd().ifPresent(clearCmd -> {
            try {
                new ProcessBuilder(BOOT_TYPE, executeMode, clearCmd)
                        .inheritIO()
                        .start()
                        .waitFor();
            } catch (Exception e) {
                // do nothing
            }
        }));
    }
}
