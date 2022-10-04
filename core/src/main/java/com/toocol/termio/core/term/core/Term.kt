package com.toocol.termio.core.term.core

import com.toocol.termio.core.Termio
import com.toocol.termio.utilities.action.AbstractDevice
import io.vertx.core.eventbus.EventBus

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/14 11:09
 */
object Term : AbstractDevice() {
    private val historyOutputInfoHelper = HistoryOutputInfoHelper.instance
    private val termPrinter: ITermPrinter
    @JvmField
    @Volatile
    var status = TermStatus.TERMIO

    @JvmField
    var theme = TermTheme.DARK_THEME

    @JvmField
    val historyCmdHelper: TermHistoryCmdHelper

    init {
        termPrinter = DesktopTermPrinter(this)
        historyCmdHelper = TermHistoryCmdHelper()
    }

    fun cleanDisplayBuffer() {
    }

    fun printScene(resize: Boolean) {
        termPrinter.printScene(resize)
    }

    fun printTermPrompt() {
        termPrinter.printTermPrompt()
    }

    fun printDisplay(msg: String?) {
        historyOutputInfoHelper.add(msg!!)
        termPrinter.printDisplay(msg)
    }

    fun printDisplayWithRecord(msg: String?) {
        termPrinter.printDisplay(msg!!)
    }

    fun printDisplayEcho(msg: String?) {
        termPrinter.printDisplayEcho(msg!!)
    }

    fun printTest() {
        termPrinter.printTest()
    }

    fun cleanDisplay() {
        termPrinter.cleanDisplay()
    }

    fun eventBus(): EventBus {
        return Termio.eventBus()
    }
}