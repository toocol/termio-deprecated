package com.toocol.ssh.utilities.log;

import com.toocol.ssh.utilities.utils.FileUtil;
import io.vertx.core.Vertx;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/28 11:55
 */
public final class LoggerFactory {

    public static void init(Vertx vertx) {
        try {
            FileUtil.checkAndCreateFile(FileAppender.FILE_PATH);
            FileAppender.vertx = vertx;
        } catch (Exception e) {
            TermioLogger.skip = true;
        }
    }

    public static Logger getLogger(Class<?> clazz) {
        return new TermioLogger(clazz);
    }

}
