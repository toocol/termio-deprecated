package com.toocol.ssh.common.utils;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:20
 */
public class PrintUtil {

    private static final int LOADING_COUNT = 3;

    public static void println(String msg) {
        System.out.println("<SSH TERMINAL> " + msg);
    }

    public static void printErr(String msg) {
        System.out.println("<ERROR> " + msg);
    }

    public static void printTitle() {
        System.out.println("\n" +
                " _____ _____ _____    _____ _____ _____ _____ _____ _____ _____ __      \n" +
                "|   __|   __|  |  |  |_   _|   __| __  |     |     |   | |  _  |  |     \n" +
                "|__   |__   |     |    | | |   __|    -| | | |-   -| | | |     |  |__   \n" +
                "|_____|_____|__|__|    |_| |_____|__|__|_|_|_|_____|_|___|__|__|_____|  \n" +
                "\n");
    }

    public static void printPromptScene() {
        printTitle();
        System.out.print("_____________________________________ Properties _____________________________________\n\n");
        System.out.print("               You have no connection properties, type 'add' to add property            \n");
        System.out.print("______________________________________________________________________________________\n\n");
        System.out.print("SSH TERMINAL is running, enter commands to operate. <enter 'help' to get more command>\n\n");
    }

    public static void printCursorLine() {
        System.out.print("[SSH TERMINAL] > ");
    }

    public static void printHelp() {
        System.out.println("SSH TERMINAL HELP: ");
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
}
