package com.toocol.termio.desktop.ui.terminal

import com.toocol.termio.platform.text.EscapedTextStyleClassArea
import com.toocol.termio.utilities.console.Console

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/2 0:43
 * @version: 0.0.1
 */
class DesktopConsole<T : EscapedTextStyleClassArea>(private val textArea: T) : Console() {

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
        textArea.cursor.setTo(textArea.calculateCursorInline(y, x))
    }

    override fun cursorBackLine(lines: Int) {}

    override fun showCursor() {
        textArea.cursor.show()
    }

    override fun hideCursor() {
        textArea.hide()
    }

    override fun cursorLeft() {
        textArea.cursor.moveLeft()
    }

    override fun cursorRight() {
        textArea.cursor.moveRight()
    }

    override fun cleanUnsupportedCharacter(bytes: ByteArray): ByteArray {
        return ByteArray(0)
    }

    override fun rollingProcessing(msg: String) {}
}