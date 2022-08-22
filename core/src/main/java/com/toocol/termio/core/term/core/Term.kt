package com.toocol.termio.core.term.core

import com.toocol.termio.core.Termio
import com.toocol.termio.utilities.action.AbstractDevice
import com.toocol.termio.utilities.ansi.AnsiStringBuilder
import com.toocol.termio.utilities.console.Console
import com.toocol.termio.utilities.utils.MessageBox
import com.toocol.termio.utilities.utils.StrUtil
import io.vertx.core.eventbus.EventBus
import jline.console.ConsoleReader
import java.io.InputStream
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/14 11:09
 */
class Term : AbstractDevice() {
    val termCharEventDispatcher: TermCharEventDispatcher

    private var termReader: ITermReader? = null
    private val historyOutputInfoHelper = HistoryOutputInfoHelper.instance
    private val termPrinter: ITermPrinter

    @JvmField
    @Volatile
    var lineBuilder = StringBuilder()

    @JvmField
    @Volatile
    var executeCursorOldX = AtomicInteger(0)

    @JvmField
    val escapeHelper: EscapeHelper

    @JvmField
    val historyCmdHelper: TermHistoryCmdHelper

    var displayZoneBottom = 0
    var lastChar = '\u0000'

    init {
        if (Termio.runType() == Termio.RunType.CONSOLE) {
            termReader = ConsoleTermReader(this)
            termPrinter = ConsoleTermPrinter(this)
        } else {
            termReader = DesktopTermReader(this)
            termPrinter = DesktopTermPrinter(this)
        }
        escapeHelper = EscapeHelper()
        historyCmdHelper = TermHistoryCmdHelper()
        termCharEventDispatcher = TermCharEventDispatcher()
    }

    fun cleanDisplayBuffer() {
        ConsoleTermPrinter.displayBuffer = StrUtil.EMPTY
    }

    fun printScene(resize: Boolean) {
        termPrinter.printScene(resize)
    }

    fun printTermPrompt() {
        termPrinter.printTermPrompt()
    }

    fun printExecution(msg: String?) {
        termPrinter.printExecution(msg!!)
    }

    fun printDisplay(msg: String?) {
        historyOutputInfoHelper.add(msg!!)
        termPrinter.printDisplay(msg)
    }

    fun printDisplayWithRecord(msg: String?) {
        termPrinter.printDisplay(msg!!)
    }

    fun printErr(msg: String?) {
        termPrinter.printDisplay(
            AnsiStringBuilder()
                .front(theme.errorMsgColor.color)
                .background(theme.displayBackGroundColor.color)
                .append(msg!!)
                .toString()
        )
    }

    fun printDisplayBuffer() {
        termPrinter.printDisplayBuffer()
    }

    fun printCommandBuffer() {
        termPrinter.printCommandBuffer()
    }

    fun printDisplayEcho(msg: String?) {
        termPrinter.printDisplayEcho(msg!!)
    }

    fun printExecuteBackground() {
        termPrinter.printExecuteBackground()
    }

    fun printTest() {
        termPrinter.printTest()
    }

    fun readLine(): String {
        return termReader!!.readLine()
    }

    val cursorPosition: IntArray
        get() {
            val coord = console!!.getCursorPosition().split(",").toTypedArray()
            return intArrayOf(coord[0].toInt(), coord[1].toInt())
        }

    fun cleanDisplay() {
        termPrinter.cleanDisplay()
    }

    fun setCursorPosition(x: Int, y: Int) {
        console!!.setCursorPosition(x, y)
    }

    fun showCursor() {
        console!!.showCursor()
    }

    fun hideCursor() {
        console!!.hideCursor()
    }

    fun cursorLeft() {
        console!!.cursorLeft()
    }

    fun cursorRight() {
        console!!.cursorRight()
    }

    fun cursorBackLine(lines: Int) {
        console!!.cursorBackLine(lines)
    }

    fun eventBus(): EventBus {
        return Termio.eventBus()
    }

    companion object {
        const val prompt = " [termio] > "
        const val topMargin = 1
        const val leftMargin = 0
        const val textLeftMargin = 1
        var console: Console? = null

        @JvmField
        val instance = Term()

        @JvmField
        @Volatile
        var status = TermStatus.TERMIO

        @JvmField
        var theme = TermTheme.DARK_THEME

        @JvmField
        var executeLine = 0

        var reader: ConsoleReader? = null

        @JvmStatic
        fun initializeReader(`in`: InputStream?) {
            try {
                reader = ConsoleReader(`in`, null, null)
            } catch (e: Exception) {
                MessageBox.setExitMessage("Create console reader failed.")
                exitProcess(-1)
            }
        }

        fun registerConsole(console: Console) {
            Term.console = console
        }

        @JvmStatic
        val promptLen: Int
            get() = prompt.length + leftMargin
    }
}