package com.toocol.termio.utilities.ansi

import com.toocol.termio.utilities.console.Console
import com.toocol.termio.utilities.utils.MessageBox
import com.toocol.termio.utilities.utils.OsUtil
import com.toocol.termio.utilities.utils.StrUtil
import java.io.PrintStream
import java.util.concurrent.CountDownLatch

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:20
 */
object Printer {
    @Volatile
    private var printer: PrintStream? = null
    private val console = Console.get()
    private val patterns = arrayOf(
        "-",
        "\\",
        "|",
        "/",
        "-"
    )

    @JvmField
    @Volatile
    var LOADING_ACCOMPLISH = false

    @JvmStatic
    fun print(msg: String?) {
        printer!!.print(msg)
    }

    fun println() {
        printer!!.println()
    }

    @JvmStatic
    fun println(msg: String?) {
        printer!!.println(msg)
    }

    @JvmStatic
    fun printErr(msg: String) {
        printer!!.println(AnsiStringBuilder()
            .front(167)
            .append(msg))
    }

    @JvmStatic
    fun bel() {
        printer!!.print(AsciiControl.BEL)
    }

    @JvmStatic
    fun virtualBackspace() {
        print(StrUtil.BACKSPACE)
        print(StrUtil.SPACE)
        print(StrUtil.BACKSPACE)
    }

    @JvmStatic
    fun virtualBackspace(cnt: Int) {
        for (i in 0 until cnt) {
            virtualBackspace()
        }
    }

    @JvmStatic
    fun printLoading(latch: CountDownLatch) {
        console.hideCursor()
        clear()
        Thread {
            var idx = 0
            print("${patterns[idx++]} starting termio.")
            try {
                while (true) {
                    if (LOADING_ACCOMPLISH) {
                        break
                    }
                    console.setCursorPosition(0, 0)
                    print(patterns[idx++])
                    if (idx >= patterns.size) {
                        idx = 1
                    }
                    Thread.sleep(200)
                }
            } catch (e: InterruptedException) {
                MessageBox.setExitMessage("Start up failed.")
                System.exit(-1)
            }
            latch.countDown()
            console.showCursor()
        }.start()
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