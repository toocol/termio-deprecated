package com.toocol.ssh.utilities.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/5/6 16:42
 */
public class MessageBox {

    private static volatile String message = StrUtil.EMPTY;

    private static volatile String errorMessage = StrUtil.EMPTY;

    private static volatile String exitMessage = StrUtil.EMPTY;

    public static boolean hasMessage() {
        return StringUtils.isNotEmpty(message);
    }

    public static boolean hasErrorMessage() {
        return StringUtils.isNotEmpty(errorMessage);
    }

    public static boolean hasExitMessage() {
        return StringUtils.isNotEmpty(exitMessage);
    }

    public static void clearMessage() {
        message = StrUtil.EMPTY;
    }

    public static void clearErrorMessage() {
        errorMessage = StrUtil.EMPTY;
    }

    public static String message() {
        return message;
    }

    public static void setMessage(String message) {
        MessageBox.message = message;
    }

    public static String errorMessage() {
        return errorMessage;
    }

    public static void setErrorMessage(String errorMessage) {
        MessageBox.errorMessage = errorMessage;
    }

    public static String exitMessage() {
        return exitMessage;
    }

    public static void setExitMessage(String exitMessage) {
        MessageBox.exitMessage = exitMessage;
    }
}
