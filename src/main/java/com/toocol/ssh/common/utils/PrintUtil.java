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
                " _____ _____ _____    _____ _____ _____ _____ _____ _____ _____ __         _____ _____ _____ _____ _____ __    \n" +
                "|   __|   __|  |  |  |_   _|   __| __  |     |     |   | |  _  |  |       |_   _|     |     |     |     |  |   \n" +
                "|__   |__   |     |    | | |   __|    -| | | |-   -| | | |     |  |__    _  | | |  |  |  |  |   --|  |  |  |__ \n" +
                "|_____|_____|__|__|    |_| |_____|__|__|_|_|_|_____|_|___|__|__|_____|  |_| |_| |_____|_____|_____|_____|_____|\n" +
                "\n");
    }

    public static void printPromptScene() {
        printTitle();
        System.out.println(" ________________________________________________ PROPERTIES ________________________________________________\n\n");
        System.out.println("                           YOU HAVE NO CONNECTION PROPERTIES, TYPE 'add' TO ADD PROPERTY\n\n");
        System.out.println(" ____________________________________________________________________________________________________________\n");
        System.out.println("SSH TERMINAL is running, enter commands to operate. <enter 'help' to get more command>\n");
    }

    public static void printCursorLine() {
        System.out.print("[SSH TERMINAL] > ");
    }

    public static void printHelp() {
        System.out.println("SSH TERMINAL HELP: ");
    }

    public static void printSelections() {
        System.out.println("Please select the boot mode: 1.[Single Window] 2.[Multiple Window]");
    }

    public static void loading() {
        try {
            System.out.print("loading");
            for (int idx = 0; idx < LOADING_COUNT; idx++) {
                Thread.sleep(550);
                System.out.print(".");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
