package com.toocol.termio.desktop.components.terminal.ui

import com.toocol.termio.platform.text.EscapedTextStyleClassArea
import com.toocol.termio.utilities.console.Console

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/2 0:43
 * @version: 0.0.1
 */
class TerminalConsole<T : EscapedTextStyleClassArea>(private val textArea: T) : Console() {
    override fun clear() {
        textArea.clear()
    }

    override fun chooseFiles(): String? {
        return null
    }

    override fun chooseDirectory(): String? {
        return null
    }

    override fun getWindowWidth(): Int {
        return 0
    }

    override fun getWindowHeight(): Int {
        return 0
    }

    override fun getCursorPosition(): String {
        val pos = textArea.getCursorPos()
        return "${pos[1]},${pos[0]}"
    }

    override fun setCursorPosition(x: Int, y: Int) {
        textArea.setCursorTo(y, x * 2)
    }

    override fun cursorBackLine(lines: Int) {}

    override fun showCursor() {
        textArea.showCursor()
    }

    override fun hideCursor() {
        textArea.hideCursor()
    }

    override fun cursorLeft() {
        textArea.cursorLeft(1)
    }

    override fun cursorRight() {
        textArea.cursorRight(1)
    }

    override fun cleanUnsupportedCharacter(bytes: ByteArray): ByteArray {
        return ByteArray(0)
    }

    override fun rollingProcessing(msg: String) {}
}