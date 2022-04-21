package com.toocol.ssh.core.term.core;

import com.toocol.ssh.common.jni.TermioJNI;
import com.toocol.ssh.common.utils.PomUtil;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.shell.commands.ShellCommand;
import com.toocol.ssh.core.term.commands.TermioCommand;

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

    private static final TermioJNI JNI = TermioJNI.getInstance();

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

    public static void printlnWithLogo(String msg) {
        println("<termio> " + msg);
    }

    public static void printErr(String msg) {
        println("<error> " + msg);
    }

    public static void printColor(String msg, int color) {
        print("\u001b[38;5;" + color + "m" + msg + "\u001b[0m");
    }

    public static void printlnColor(String msg, int color) {
        println("\u001b[38;5;" + color + "m" + msg + "\u001b[0m");
    }

    public static void printColorBackground(String msg, int color) {
        print("\u001b[48;5;" + color + "m" + msg + "\u001b[0m");
    }

    public static void virtualBackspace() {
        print("\b");
        print(" ");
        print("\b");
    }

    public static void printTitleAndInfo() {
        println("termio\t\tv" + PomUtil.getVersion() + "\n" +
                "website\t\thttps://github.com/Joezeo/termio\n" +
                "memory use\ttotal:" + totalMemory() + "MB, max:" + maxMemory() + "MB, free:" + freeMemory() + "MB, used:" + usedMemory() + "MB\n" +
                "active\t\t" + SessionCache.getHangUp() + "\n"
        );
    }

    public static void printScene() {
        printTitleAndInfo();
        print("Properties:                                                                           \n");
        if (CredentialCache.credentialsSize() == 0) {
            print("You have no connection properties, type 'help' to get more information.                         \n\n");
        } else {
            CredentialCache.showCredentials();
            println();
        }
    }

    @SuppressWarnings("all")
    public static void printLoading(CountDownLatch latch) {
        JNI.hideCursor();
        clear();
        new Thread(() -> {
            int idx = 0;
            print(patterns[idx++] + " starting termio.");
            try {
                while (true) {
                    if (StatusCache.LOADING_ACCOMPLISH) {
                        break;
                    }
                    JNI.setCursorPosition(0, 0);
                    ;
                    print(patterns[idx++]);
                    if (idx >= patterns.length) {
                        idx = 1;
                    }
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                print("Start up failed.");
                System.exit(-1);
            }
            latch.countDown();
            JNI.showCursor();
        }).start();
    }

    public static void printPrompt(String wrongCmd) {
        println("" + wrongCmd + ": command not found.");
    }

    public static void printCursorLine() {
        print("[termio] > ");
    }

    public static void printHelp() {
        TermioCommand.printHelp();
        ShellCommand.printHelp();
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
