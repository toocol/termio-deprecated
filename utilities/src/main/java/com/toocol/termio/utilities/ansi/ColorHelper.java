package com.toocol.termio.utilities.ansi;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/18 13:43
 */
public final class ColorHelper {
    /**
     * 256-color mode.
     */
    public static String front(String msg, int color) {
        return "\u001b[38;5;" + color + "m" + msg + "\u001b[0m";
    }

    public static String background(String msg, int color) {
        return "\u001b[48;5;" + color + "m" + msg + "\u001b[0m";
    }

    /**
     * RGB-color mode
     */
    public static String front(String msg, int r, int g, int b) {
        return "\u001b[38;2;" + r + ";" + g + ";" + b + "m" + msg + "\u001b[0m";
    }

    public static String background(String msg, int r, int g, int b) {
        return "\u001b[48;2;" + r + ";" + g + ";" + b + "m" + msg + "\u001b[0m";
    }
}
