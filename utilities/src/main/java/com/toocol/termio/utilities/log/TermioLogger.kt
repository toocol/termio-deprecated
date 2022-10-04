package com.toocol.termio.utilities.log

import com.toocol.termio.utilities.log.FileAppender.logFileAppend
import com.toocol.termio.utilities.utils.StrUtil
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/28 11:52
 */
object TermioLogger : Logger {
    private const val DEBUG = "DEBUG"
    private const val INFO = "INFO"
    private const val WARN = "WARN"
    private const val ERROR = "ERROR"

    private var skip = true

    private val logBuilder = StringBuilder()
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    fun skip() {
        skip = true
    }

    fun nonSkip() {
        skip = false
    }

    override fun debug(className: String, message: String?, vararg params: Any?) {
        if (skip) {
            return
        }
        log(className, message, DEBUG, *params)
    }

    override fun info(className: String, message: String?, vararg params: Any?) {
        if (skip) {
            return
        }
        log(className, message, INFO, *params)
    }

    override fun warn(className: String, message: String?, vararg params: Any?) {
        if (skip) {
            return
        }
        log(className, message, WARN, *params)
    }

    override fun error(className: String, message: String?, vararg params: Any?) {
        if (skip) {
            return
        }
        log(className, message, ERROR, *params)
    }

    @Synchronized
    private fun log(className: String, message: String?, level: String, vararg params: Any?) {
        logBuilder.delete(0, logBuilder.length)
        appendTime().append(" ").append(level).append(" ")
        appendClassThreadInfo(className).append(StrUtil.fullFillParam(message, *params)).append("\r\n")
        logFileAppend(logBuilder.toString())
    }

    private fun appendTime(): StringBuilder {
        return logBuilder.append(simpleDateFormat.format(Date()))
    }

    private fun appendClassThreadInfo(className: String): StringBuilder {
        return logBuilder.append("[").append(className).append(",").append(" ")
            .append(Thread.currentThread().name).append("]").append(" ")
    }
}