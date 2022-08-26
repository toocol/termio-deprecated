package com.toocol.termio.utilities.console

import com.toocol.termio.utilities.utils.OsUtil

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/24 11:45
 */
abstract class Console {
    abstract fun clear()

    abstract fun chooseFiles(): String?

    abstract fun chooseDirectory(): String?

    abstract fun getWindowWidth(): Int

    abstract fun getWindowHeight(): Int

    abstract fun getCursorPosition(): String

    abstract fun setCursorPosition(x: Int, y: Int)

    abstract fun cursorBackLine(lines: Int)

    abstract fun showCursor()

    abstract fun hideCursor()

    abstract fun cursorLeft()

    abstract fun cursorRight()

    abstract fun cleanUnsupportedCharacter(bytes: ByteArray): ByteArray

    abstract fun rollingProcessing(msg: String)

    companion object {
        private var console: Console? = null
        @JvmStatic
        @Synchronized
        fun get(): Console {
            if (console != null) {
                return console!!
            }
            console = if (OsUtil.isWindows()) {
                WindowsConsole()
            } else {
                UnixConsole()
            }
            return console!!
        }
    }
}