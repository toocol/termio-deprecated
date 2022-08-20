package com.toocol.termio.core.term.core

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/20 22:37
 * @version: 0.0.1
 */
interface ITermPrinter {

    fun printScene(resize: Boolean)

    fun printTermPrompt()

    fun printExecuteBackground()

    fun printExecution(msg: String)

    fun cleanDisplay()

    fun printDisplay(msg: String)

    fun printDisplayEcho(msg: String)

    fun printDisplayBackground(lines: Int)

    fun printDisplayBuffer()

    fun printCommandBuffer()

    fun printTest()
}