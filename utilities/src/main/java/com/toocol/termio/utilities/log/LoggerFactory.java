package com.toocol.termio.utilities.log;

import com.toocol.termio.utilities.utils.FileUtil;
import io.vertx.core.Vertx;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/28 11:55
 */
public final class LoggerFactory {

    private static final Map<Class<?>, Logger> LOGGER_MAP = new ConcurrentHashMap<>();

    public static void init(Vertx vertx) {
        try {
            FileUtil.checkAndCreateFile(FileAppender.filePath());
            FileAppender.openLogFile(vertx);
            TermioLogger.nonSkip();
        } catch (Exception e) {
            TermioLogger.skip();
        }
    }

    public static Logger getLogger(Class<?> clazz) {
        return LOGGER_MAP.compute(clazz, (k, v) -> {
            if (v == null) {
                v = new TermioLogger(clazz, new StringBuilder(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            }
            return v;
        });
    }

}
