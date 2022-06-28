package com.toocol.ssh.utilities.log;

import com.toocol.ssh.utilities.utils.StrUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/28 11:52
 */
public record TermioLogger(Class<?> clazz) implements Logger {

    private static final String INFO = "INFO";
    private static final String WARN = "WARN";
    private static final String ERROR = "ERROR";

    private static boolean skip = true;

    @Override
    public void info(String message, Object... params) {
        if (skip) {
            return;
        }
        log(message, INFO, params);
    }

    @Override
    public void warn(String message, Object... params) {
        if (skip) {
            return;
        }
        log(message, WARN, params);
    }

    @Override
    public void error(String message, Object... params) {
        if (skip) {
            return;
        }
        log(message, ERROR, params);
    }

    public static void skip() {
        skip = true;
    }

    public static void nonSkip() {
        skip = false;
    }

    private void log(String message, String level, Object... params) {
        final StringBuilder logBuilder = new StringBuilder();
        appendTime(logBuilder).append(" ").append(level).append(" ");
        appendClassThreadInfo(logBuilder).append(StrUtil.fullFillParam(message, params)).append("\r\n");
        FileAppender.logFileAppend(logBuilder.toString());
    }

    private StringBuilder appendTime(final StringBuilder logBuilder) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return logBuilder.append(simpleDateFormat.format(new Date()));
    }

    private StringBuilder appendClassThreadInfo(final StringBuilder logBuilder) {
        return logBuilder.append("[").append(clazz.getSimpleName()).append(",").append(" ")
                .append(Thread.currentThread().getName()).append("]").append(" ");
    }
}
