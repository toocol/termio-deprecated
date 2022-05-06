package com.toocol.ssh.utilities.utils;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/5/6 16:42
 */
public class ExitMessage {

    private static volatile String msg = StrUtil.EMPTY;

    public static String getMsg() {
        return msg;
    }

    public static void setMsg(String msg) {
        ExitMessage.msg = msg;
    }
}
