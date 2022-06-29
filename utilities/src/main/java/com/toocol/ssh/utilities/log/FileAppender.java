package com.toocol.ssh.utilities.log;

import com.toocol.ssh.utilities.status.StatusCache;
import com.toocol.ssh.utilities.utils.FileUtil;
import io.vertx.core.Promise;
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

    private static final String FILE_PATH = "./termio.log";
    private static final Queue<String> LOG_QUEUE = new ConcurrentLinkedDeque<>();

    protected static String filePath() {
        return FileUtil.relativeToFixed(FILE_PATH);
    }

    private static volatile boolean opened = false;

    @SuppressWarnings("all")
    protected static void openLogFile(Vertx vertx) {
        vertx.fileSystem().open(filePath(), new OpenOptions().setAppend(true), ar -> {
            if (ar.succeeded()) {
                opened = true;
                AsyncFile ws = ar.result();
                new Thread(() -> {
                    while (true) {
                        while (!LOG_QUEUE.isEmpty()) {
                            ws.write(Buffer.buffer(LOG_QUEUE.poll()));
                        }

                        if (StatusCache.STOP_PROGRAM) {
                            ws.close();
                            break;
                        }

                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    protected static void logFileAppend(String log) {
        if (!opened) {
            return;
        }
        LOG_QUEUE.offer(log);
    }

}
