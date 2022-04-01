package com.toocol.ssh.common.utils;

import com.toocol.ssh.core.cache.Cache;
import com.toocol.ssh.core.command.commands.OutsideCommand;
import com.toocol.ssh.core.credentials.vo.SshCredential;
import com.toocol.ssh.core.shell.commands.ShellCommand;

import java.io.PrintStream;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.toocol.ssh.core.configuration.SystemConfiguration.*;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:20
 */
public class Printer {

    private static final PrintStream PRINT = System.out;

    private static final int LOADING_COUNT = 3;

    public static void print(String msg) {
        PRINT.print(msg);
    }

    public static void println() {
        PRINT.println();
    }

    public static void println(String msg) {
        PRINT.println(msg);
    }

    public static void printlnWithLogo(String msg) {
        PRINT.println("<ssh terminal> " + msg);
    }

    public static void printErr(String msg) {
        PRINT.println("<ERROR> " + msg);
    }

    public static void printTitle() {
        PRINT.println("\n" +
                " _____ _____ _____    _____ _____ _____ _____ _____ _____ _____ __                    \n" +
                "|   __|   __|  |  |  |_   _|   __| __  |     |     |   | |  _  |  |                   \n" +
                "|__   |__   |     |    | | |   __|    -| | | |-   -| | | |     |  |__                 \n" +
                "|_____|_____|__|__|    |_| |_____|__|__|_|_|_|_____|_|___|__|__|_____|                \n" +
                "\n");
    }

    public static void printScene() {
        printTitle();
        PRINT.print("Properties:                                                                           \n");
        if (Cache.credentialsSize() == 0) {
            PRINT.print("You have no connection properties, type 'help' to get more information.                         \n\n");
        } else {
            Cache.showCredentials();
            PRINT.println();
        }
    }

    public static void printPrompt(String wrongCmd) {
        PRINT.print("'" + wrongCmd + "' is not a command, enter 'help' to get more information.\n");
    }

    public static void printCursorLine() {
        PRINT.print("[ssh terminal] > ");
    }

    public static void printHelp() {
        OutsideCommand.printHelp();
        ShellCommand.printHelp();
    }

    public static void loading() {
        try {
            PRINT.print("loading");
            for (int idx = 0; idx < LOADING_COUNT; idx++) {
                Thread.sleep(400);
                PRINT.print(".");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void clear() {
        getExecuteMode().ifPresent(executeMode -> {
            getClearCmd().ifPresent(clearCmd -> {
                try {
                    new ProcessBuilder(BOOT_TYPE, executeMode, clearCmd)
                            .inheritIO()
                            .start()
                            .waitFor();
                } catch (Exception e) {
                    // do nothing
                }
            });
        });
    }
}
