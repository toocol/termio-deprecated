package com.toocol.ssh.utilities.execeptions;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/7/1 10:36
 */
public interface IStacktraceParser {

    default String parseStackTrace(Exception e) {
        StringBuilder stackTraceBuilder = new StringBuilder();
        stackTraceBuilder.append("\r\n").append("\t").append(e.getClass().getName()).append("\r\n");
        for (int i = 0; i < e.getStackTrace().length; i++) {
            StackTraceElement stackTraceElement = e.getStackTrace()[i];
            stackTraceBuilder.append("\t").append(" - ").append(stackTraceElement.toString());
            if (i != e.getStackTrace().length - 1) {
                stackTraceBuilder.append("\r\n");
            }
        }
        return stackTraceBuilder.toString();
    }

}
