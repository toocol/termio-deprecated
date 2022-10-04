package com.toocol.termio.utilities.log;

import com.toocol.termio.utilities.execeptions.IStacktraceParser;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/28 15:56
 */
public interface Loggable extends IStacktraceParser {

    default void debug(String message, Object... params) {
        Logger logger = LoggerFactory.getLogger();
        logger.debug(this.getClass().getSimpleName(), message, params);
    }

    default void info(String message, Object... params) {
        Logger logger = LoggerFactory.getLogger();
        logger.info(this.getClass().getSimpleName(), message, params);
    }

    default void warn(String message, Object... params) {
        Logger logger = LoggerFactory.getLogger();
        logger.warn(this.getClass().getSimpleName(), message, params);
    }

    default void error(String message, Object... params) {
        Logger logger = LoggerFactory.getLogger();
        logger.error(this.getClass().getSimpleName(), message, params);
    }

}
