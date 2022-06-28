package com.toocol.ssh.utilities.log;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/28 14:33
 */
public class FileAppender {

    protected static final String FILE_PATH = "./termio.log";

    protected static Vertx vertx;

    protected static void logFileAppend(String log) {
        if (vertx == null) {
            return;
        }
        vertx.fileSystem().open(FILE_PATH, new OpenOptions().setAppend(true), ar -> {
            if (ar.succeeded()) {
                AsyncFile ws = ar.result();
                ws.write(Buffer.buffer(log));
                ws.close();
            }
        });
    }

}
