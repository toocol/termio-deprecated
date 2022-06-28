package com.toocol.ssh.utilities.log;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/28 15:56
 */
public interface Loggable {

    default void info(String message, Object... params) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info(message, params);
    }

    default void warn(String message, Object... params) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.warn(message, params);
    }

    default void error(String message, Object... params) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.error(message, params);
    }

}
