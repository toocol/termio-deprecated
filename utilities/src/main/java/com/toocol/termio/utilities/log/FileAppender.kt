package com.toocol.termio.utilities.log

import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.jvm.Volatile
import com.toocol.termio.utilities.utils.FileUtil
import io.vertx.core.Vertx
import com.toocol.termio.utilities.utils.StrUtil
import io.vertx.core.file.OpenOptions
import io.vertx.core.AsyncResult
import io.vertx.core.Promise
import io.vertx.core.buffer.Buffer
import io.vertx.core.file.AsyncFile
import java.lang.InterruptedException
import java.util.*

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/28 14:33
 */
object FileAppender {
    private const val defaultFilePath = "./"
    private const val defaultFileName = "termio.log"
    private val logQueue: Queue<String> = ConcurrentLinkedDeque()

    @Volatile
    private var opened = false
    @JvmStatic
    fun filePath(): String {
        return FileUtil.relativeToFixed(defaultFilePath)
    }

    @JvmStatic
    fun close() {
        opened = false
    }

    @JvmStatic
    fun openLogFile(vertx: Vertx) {
        val logFile = System.getProperty("logFile")
        val fileName = if (StrUtil.isEmpty(logFile)) defaultFileName else logFile
        vertx.fileSystem()
            .open(defaultFilePath + fileName, OpenOptions().setAppend(true)) { ar: AsyncResult<AsyncFile> ->
                if (ar.succeeded()) {
                    opened = true
                    val ws = ar.result()
                    vertx.executeBlocking { promise: Promise<Any?> ->
                        while (true) {
                            while (!logQueue.isEmpty()) {
                                ws.write(Buffer.buffer(logQueue.poll()))
                            }
                            if (!opened) {
                                ws.close()
                                break
                            }
                            try {
                                Thread.sleep(1)
                            } catch (e: InterruptedException) {
                                // do nothing
                            }
                        }
                        promise.complete()
                    }
                }
            }
    }

    @JvmStatic
    fun logFileAppend(log: String) {
        logQueue.offer(log)
    }
}