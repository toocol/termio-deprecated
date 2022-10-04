package com.toocol.termio.utilities.ansi

import com.toocol.termio.utilities.utils.OsUtil
import com.toocol.termio.utilities.utils.StrUtil
import java.io.PrintStream

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:20
 */
object Printer {
    @Volatile
    private var printer: PrintStream? = null

    @JvmField
    @Volatile
    var LOADING_ACCOMPLISH = false

    @JvmStatic
    fun print(msg: String?) {
        printer!!.print(msg?.replace(StrUtil.CRLF, StrUtil.LF))
    }

    fun println() {
        printer!!.print(StrUtil.LF)
    }

    @JvmStatic
    fun println(msg: String?) {
        printer!!.print(msg?.replace(StrUtil.CRLF, StrUtil.LF) + StrUtil.LF)
    }

    @JvmStatic
    fun printErr(msg: String) {
        printer!!.println(AnsiStringBuilder()
            .front(167)
            .append(msg.replace(StrUtil.CRLF, StrUtil.LF) + StrUtil.LF))
    }

    @JvmStatic
    fun bel() {
        printer!!.print(AsciiControl.BEL)
    }

    @JvmStatic
    fun virtualBackspace() {
        print(StrUtil.VIRTUAL_BACKSPACE)
    }

    @JvmStatic
    fun clear() {
        try {
            ProcessBuilder(OsUtil.getExecution(), OsUtil.getExecuteMode(), OsUtil.getClearCmd())
                .inheritIO()
                .start()
                .waitFor()
        } catch (e: Exception) {
            // do nothing
        }
    }

    @JvmStatic
    fun setPrinter(printer: PrintStream?) {
        this.printer = printer
    }
}