package com.toocol.ssh.utilities.log;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/28 11:55
 */
public interface Logger {
    void info(String message, Object... params);

    void warn(String message, Object... params);

    void error(String message, Object... params);
}
