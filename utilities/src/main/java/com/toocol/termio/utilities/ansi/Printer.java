package com.toocol.termio.utilities.ansi;

import com.toocol.termio.utilities.console.Console;
import com.toocol.termio.utilities.utils.MessageBox;
import com.toocol.termio.utilities.utils.OsUtil;
import com.toocol.termio.utilities.utils.StrUtil;

import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:20
 */
public final class Printer {
    private static PrintStream PRINTER;
    private static final Console CONSOLE = Console.get();
    private static final String[] patterns = new String[]{
            "-",
            "\\",
            "|",
            "/",
            "-"
    };
    public volatile static boolean LOADING_ACCOMPLISH = false;

    public static void print(String msg) {
        PRINTER.print(msg);
    }

    public static void println() {
        PRINTER.println();
    }

    public static void println(String msg) {
        PRINTER.println(msg);
    }

    public static void printErr(String msg) {
        PRINTER.println(new AnsiStringBuilder()
                .front(167)
                .append(msg));
    }

    public static void bel() {
        PRINTER.print(AsciiControl.BEL);
    }

    public static void virtualBackspace() {
        print(StrUtil.BACKSPACE);
        print(StrUtil.SPACE);
        print(StrUtil.BACKSPACE);
    }

    public static void virtualBackspace(int cnt) {
        for (int i = 0; i < cnt; i++) {
            virtualBackspace();
        }
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
                    if (LOADING_ACCOMPLISH) {
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
                MessageBox.setExitMessage("Start up failed.");
                System.exit(-1);
            }
            latch.countDown();
            CONSOLE.showCursor();
        }).start();
    }

    public static void clear() {
        try {
            new ProcessBuilder(OsUtil.getExecution(), OsUtil.getExecuteMode(), OsUtil.getClearCmd())
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (Exception e) {
            // do nothing
        }
    }

    public static void setPrinter(PrintStream printer) {
        Printer.PRINTER = printer;
    }
}
