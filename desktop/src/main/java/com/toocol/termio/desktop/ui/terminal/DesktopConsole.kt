package com.toocol.termio.desktop.ui.terminal

import com.toocol.termio.utilities.console.Console

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/2 0:43
 * @version: 0.0.1
 */
class DesktopConsole : Console() {
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
        return "0,0"
    }

    override fun setCursorPosition(x: Int, y: Int) {}
    override fun cursorBackLine(lines: Int) {}
    override fun showCursor() {}
    override fun hideCursor() {}
    override fun cursorLeft() {}
    override fun cursorRight() {}
    override fun cleanUnsupportedCharacter(bytes: ByteArray): ByteArray {
        return ByteArray(0)
    }

    override fun rollingProcessing(msg: String) {}
}