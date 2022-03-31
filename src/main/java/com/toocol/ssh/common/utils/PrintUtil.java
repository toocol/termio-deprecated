package com.toocol.ssh.common.utils;

import com.toocol.ssh.core.command.commands.OutsideCommand;
import com.toocol.ssh.core.credentials.vo.SshCredential;

import java.util.List;

import static com.toocol.ssh.core.configuration.vert.ConfigurationVerticle.*;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:20
 */
public class PrintUtil {

    private static final int LOADING_COUNT = 3;

    public static void println(String msg) {
        System.out.println("<ssh terminal> " + msg);
    }

    public static void printErr(String msg) {
        System.out.println("<ERROR> " + msg);
    }

    public static void printTitle() {
        System.out.println("\n" +
                " _____ _____ _____    _____ _____ _____ _____ _____ _____ _____ __                    \n" +
                "|   __|   __|  |  |  |_   _|   __| __  |     |     |   | |  _  |  |                   \n" +
                "|__   |__   |     |    | | |   __|    -| | | |-   -| | | |     |  |__                 \n" +
                "|_____|_____|__|__|    |_| |_____|__|__|_|_|_|_____|_|___|__|__|_____|                \n" +
                "\n");
    }

    public static void printScene(List<SshCredential> credentials) {
        printTitle();
        System.out.print("Properties:                                                                           \n");
        System.out.print("You have no connection properties, type 'add' to add property                         \n\n");
    }

    public static void printPrompt(String wrongCmd) {
        System.out.print("'" + wrongCmd + "' is not a command, enter 'help' to get more command\n");
    }

    public static void printCursorLine() {
        System.out.print("[ssh terminal] > ");
    }

    public static void printHelp() {
        OutsideCommand.printHelp();
    }

    public static void loading() {
        try {
            System.out.print("loading");
            for (int idx = 0; idx < LOADING_COUNT; idx++) {
                Thread.sleep(450);
                System.out.print(".");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void clear() throws Exception {
        new ProcessBuilder(BOOT_TYPE, getExtraCmd(), getClearCmd())
                .inheritIO()
                .start()
                .waitFor();
    }
}
