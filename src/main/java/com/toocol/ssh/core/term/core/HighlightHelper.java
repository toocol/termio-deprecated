package com.toocol.ssh.core.term.core;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/18 13:43
 */
public final class HighlightHelper {

    public static final int EXECUTE_LINE_BACKGROUND = 234;
    public static final int COMMAND_HIGHLIGHT_COLOR = 43;
    public static final int ALIVE_STATUS_COLOR = 43;
    public static final int HOST_HIGHLIGHT_COLOR = 229;

    public static String assembleColor(String msg, int color) {
        return "\u001b[38;5;" + color + "m" + msg + "\u001b[0m";
    }

    public static String assembleColorBackground(String msg, int color) {
       return "\u001b[48;5;" + color + "m" + msg + "\u001b[0m";
    }

}
