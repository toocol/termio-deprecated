package com.toocol.ssh.common.utils;

import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.term.commands.OutsideCommand;
import com.toocol.ssh.core.shell.commands.ShellCommand;

import java.io.PrintStream;

import static com.toocol.ssh.core.config.SystemConfig.*;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:20
 */
public class Printer {
    private static final PrintStream PRINT = System.out;

    private static final Runtime RUNTIME = Runtime.getRuntime();

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
        PRINT.print(msg);
    }

    public static void println() {
        PRINT.println();
    }

    public static void println(String msg) {
        PRINT.println(msg);
    }

    public static void printlnWithLogo(String msg) {
        PRINT.println("<terminatio> " + msg);
    }

    public static void printErr(String msg) {
        PRINT.println("<error> " + msg);
    }

    public static void virtualBackspace() {
        Printer.print("\b");
        Printer.print(" ");
        Printer.print("\b");
    }

    public static void printTitleAndInfo() {
        PRINT.println("terminatio\tv" + PomUtil.getVersion() + "\n" +
                "website\t\thttps://github.com/Joezeo/terminatio\n" +
                "os\t\t" + System.getProperty("os.name") + "\n" +
                "shell env\t" + BOOT_TYPE + "\n" +
                "memory use\ttotal:" + totalMemory() + "MB, max:" + maxMemory() + "MB, free:" + freeMemory() + "MB, used:" + usedMemory() + "MB\n" +
                "hang-up\t\t" + SessionCache.getHangUp() + "\n"
        );
    }

    public static void printScene() {
        printTitleAndInfo();
        PRINT.print("Properties:                                                                           \n");
        if (CredentialCache.credentialsSize() == 0) {
            PRINT.print("You have no connection properties, type 'help' to get more information.                         \n\n");
        } else {
            CredentialCache.showCredentials();
            PRINT.println();
        }
    }

    public static void printPrompt(String wrongCmd) {
        PRINT.print("'" + wrongCmd + "' is not a command, enter 'help' to get more information.\n");
    }

    public static void printCursorLine() {
        PRINT.print("[terminatio] > ");
    }

    public static void printHelp() {
        OutsideCommand.printHelp();
        ShellCommand.printHelp();
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
