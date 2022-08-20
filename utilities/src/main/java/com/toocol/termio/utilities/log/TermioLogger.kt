package com.toocol.termio.utilities.log

import com.toocol.termio.utilities.log.FileAppender.logFileAppend
import com.toocol.termio.utilities.utils.StrUtil
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/28 11:52
 */
class TermioLogger(private val clazz: Class<*>, private val logBuilder: StringBuilder, private val simpleDateFormat: SimpleDateFormat): Logger {
    companion object {
        private const val DEBUG = "DEBUG"
        private const val INFO = "INFO"
        private const val WARN = "WARN"
        private const val ERROR = "ERROR"
        private var skip = true
        fun skip() {
            skip = true
        }

        fun nonSkip() {
            skip = false
        }
    }

    override fun debug(message: String?, vararg params: Any?) {
        if (skip) {
            return
        }
        log(message, DEBUG, *params)
    }

    override fun info(message: String?, vararg params: Any?) {
        if (skip) {
            return
        }
        log(message, INFO, *params)
    }

    override fun warn(message: String?, vararg params: Any?) {
        if (skip) {
            return
        }
        log(message, WARN, *params)
    }

    override fun error(message: String?, vararg params: Any?) {
        if (skip) {
            return
        }
        log(message, ERROR, *params)
    }

    @Synchronized
    private fun log(message: String?, level: String, vararg params: Any?) {
        logBuilder.delete(0, logBuilder.length)
        appendTime(logBuilder).append(" ").append(level).append(" ")
        appendClassThreadInfo(logBuilder).append(StrUtil.fullFillParam(message, *params)).append("\r\n")
        logFileAppend(logBuilder.toString())
    }

    private fun appendTime(logBuilder: StringBuilder): StringBuilder {
        return logBuilder.append(simpleDateFormat.format(Date()))
    }

    private fun appendClassThreadInfo(logBuilder: StringBuilder): StringBuilder {
        return logBuilder.append("[").append(clazz.simpleName).append(",").append(" ")
            .append(Thread.currentThread().name).append("]").append(" ")
    }
}