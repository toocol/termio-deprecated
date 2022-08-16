package com.toocol.termio.utilities.log;

import com.toocol.termio.utilities.utils.FileUtil;
import com.toocol.termio.utilities.utils.StrUtil;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/28 14:33
 */
public class FileAppender {

    private static final String DEFAULT_FILE_PATH = "./";
    private static final String DEFAULT_FILE_NAME = "termio.log";

    private static final Queue<String> LOG_QUEUE = new ConcurrentLinkedDeque<>();
    private static volatile boolean opened = false;

    protected static String filePath() {
        return FileUtil.relativeToFixed(DEFAULT_FILE_PATH);
    }

    public static void close() {
        opened = false;
    }

    @SuppressWarnings("all")
    protected static void openLogFile(Vertx vertx) {
        String logFile = System.getProperty("logFile");
        String fileName = StrUtil.isEmpty(logFile) ? DEFAULT_FILE_PATH : logFile;

        vertx.fileSystem().open(DEFAULT_FILE_PATH + fileName, new OpenOptions().setAppend(true), ar -> {
            if (ar.succeeded()) {
                opened = true;
                AsyncFile ws = ar.result();
                vertx.executeBlocking(promise -> {
                    while (true) {
                        while (!LOG_QUEUE.isEmpty()) {
                            ws.write(Buffer.buffer(LOG_QUEUE.poll()));
                        }

                        if (!opened) {
                            ws.close();
                            break;
                        }

                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            // do nothing
                        }
                    }
                    promise.complete();
                });
            }
        });
    }

    protected static void logFileAppend(String log) {
        LOG_QUEUE.offer(log);
    }

}
