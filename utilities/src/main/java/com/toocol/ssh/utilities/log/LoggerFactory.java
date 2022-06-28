package com.toocol.ssh.utilities.log;

import com.toocol.ssh.utilities.utils.FileUtil;
import io.vertx.core.Vertx;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/28 11:55
 */
public final class LoggerFactory {

    private static final Map<Class<?>, Logger> LOGGER_MAP = new HashMap<>();

    public static void init(Vertx vertx) {
        try {
            FileUtil.checkAndCreateFile(FileAppender.FILE_PATH);
            FileAppender.vertx = vertx;
            TermioLogger.nonSkip();
        } catch (Exception e) {
            TermioLogger.skip();
        }
    }

    public static Logger getLogger(Class<?> clazz) {
        return LOGGER_MAP.compute(clazz, (k, v) -> {
            if (v == null) {
                v = new TermioLogger(clazz);
            }
            return v;
        });
    }

}
