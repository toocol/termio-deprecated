package com.toocol.ssh.utilities.anis;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/18 13:43
 */
public final class HighlightHelper {

    public static String assembleColor(String msg, int color) {
        return "\u001b[38;5;" + color + "m" + msg + "\u001b[0m";
    }

    public static String assembleColorBackground(String msg, int color) {
       return "\u001b[48;5;" + color + "m" + msg + "\u001b[0m";
    }

    public static String assembleColorBoth(String msg, int front, int background) {
        return "\u001b[48;5;" + background + "m\u001b[38;5;" + front + "m" + msg + "\u001b[0m\u001b[0m";
    }

}
