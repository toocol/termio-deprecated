package com.toocol.termio.utilities.log

import com.toocol.termio.utilities.log.FileAppender.filePath
import com.toocol.termio.utilities.log.FileAppender.openLogFile
import com.toocol.termio.utilities.utils.FileUtil

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/28 11:55
 */
object LoggerFactory {
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
    fun getLogger(): Logger {
        return TermioLogger
    }
}