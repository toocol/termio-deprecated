package com.toocol.termio.utilities.log

import com.toocol.termio.utilities.log.FileAppender.filePath
import com.toocol.termio.utilities.log.FileAppender.openLogFile
import com.toocol.termio.utilities.utils.FileUtil
import java.text.SimpleDateFormat
import java.util.concurrent.ConcurrentHashMap

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/28 11:55
 */
object LoggerFactory {
    private val loggerMap: java.util.AbstractMap<Class<*>, Logger> = ConcurrentHashMap()

    @JvmStatic
    fun init() {
        try {
            FileUtil.checkAndCreateFile(filePath())
            openLogFile()
            TermioLogger.nonSkip()
        } catch (e: Exception) {
            TermioLogger.skip()
        }
    }

    @JvmStatic
    fun getLogger(clazz: Class<*>): Logger {
        var logger = loggerMap[clazz]
        if (logger == null) {
            logger = TermioLogger(clazz, StringBuilder(), SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
            loggerMap[clazz] = logger
        }
        return logger
    }
}