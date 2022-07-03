package com.toocol.ssh.core.term.core;

import com.toocol.ssh.utilities.anis.AnisStringBuilder;
import com.toocol.ssh.utilities.console.Console;
import com.toocol.ssh.utilities.status.StatusCache;
import com.toocol.ssh.utilities.utils.ExitMessage;

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

    private static final Console CONSOLE = Console.get();

    private static final String[] patterns = new String[]{"-",
            "\\",
            "|",
            "/",
            "-"};

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
        println(
                new AnisStringBuilder()
                        .front(Term.theme.errorMsgColor)
                        .append(msg)
                        .toString()
        );
    }

    public static void virtualBackspace() {
        print("\b");
        print(" ");
        print("\b");
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
