package com.toocol.termio.utilities.log

import com.toocol.termio.utilities.utils.FileUtil
import com.toocol.termio.utilities.utils.StrUtil
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

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
    fun openLogFile() {
        val logFile = System.getProperty("logFile")
        val fileName = if (StrUtil.isEmpty(logFile)) defaultFileName else logFile
        File(defaultFilePath + fileName).run {
            val logThread = Thread({
                if (!exists()) {
                    createNewFile()
                }
                try {
                    opened = true
                    while (true) {
                        while (!logQueue.isEmpty()) {
                            appendText(logQueue.poll())
                        }
                        if (!opened) {
                            return@Thread
                        }
                        try {
                            Thread.sleep(1)
                        } catch (e: InterruptedException) {
                            // do nothing
                        }
                    }
                } finally {
                    close()
                }
            }, "log-appender")
            logThread.isDaemon = true
            logThread.start()
        }
    }

    @JvmStatic
    fun logFileAppend(log: String) {
        print(log)
        logQueue.offer(log)
    }
}