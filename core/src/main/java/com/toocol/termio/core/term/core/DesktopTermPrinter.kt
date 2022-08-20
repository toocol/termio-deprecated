package com.toocol.termio.core.term.core

import com.toocol.termio.utilities.escape.esc2J
import com.toocol.termio.utilities.utils.StrUtil
import java.io.PrintStream

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/20 22:08
 * @version: 0.0.1
 */
class DesktopTermPrinter(private val term: Term) : ITermPrinter {
    companion object {
        @Volatile
        private var printStream: PrintStream? = null

        @JvmStatic
        fun registerPrintStream(printStream: PrintStream) {
            this.printStream = printStream
        }
    }

    private fun println(msg: String) {
        printStream!!.print(msg + StrUtil.LF)
    }

    override fun cleanDisplay() {
        printStream!!.print(esc2J)
    }

    override fun printDisplay(msg: String) {
        cleanDisplay()
        println(msg)
    }

    override fun printDisplayEcho(msg: String) {
        cleanDisplay()
        println(msg)
    }

    /* Following methods is useless in Desktop version */
    override fun printScene(resize: Boolean) {
    }

    override fun printTermPrompt() {
    }

    override fun printExecuteBackground() {
    }

    override fun printExecution(msg: String) {
    }

    override fun printDisplayBackground(lines: Int) {
    }

    override fun printDisplayBuffer() {
    }

    override fun printCommandBuffer() {
    }

    override fun printTest() {
    }
}