package com.toocol.termio.core.term.core

import com.toocol.termio.core.shell.core.Shell
import com.toocol.termio.utilities.console.IConsoleReader
import com.toocol.termio.utilities.utils.CharUtil

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/18 22:53
 * @version: 0.0.1
 */
class EscapeHelper {
    private var acceptEscape = false
    var isAcceptBracketAfterEscape = false
        private set

    fun processArrowStream(inCharConst: Char): Char {
        var inChar = inCharConst
        if (inChar == CharUtil.ESCAPE) {
            acceptEscape = true
        }
        if (acceptEscape) {
            if (inChar == CharUtil.BRACKET_START) {
                isAcceptBracketAfterEscape = true
            } else if (inChar != CharUtil.ESCAPE) {
                acceptEscape = false
            }
        }
        if (isAcceptBracketAfterEscape && inChar != CharUtil.BRACKET_START) {
            acceptEscape = false
            isAcceptBracketAfterEscape = false
            inChar = when (inChar) {
                'A' -> CharUtil.UP_ARROW
                'B' -> CharUtil.DOWN_ARROW
                'C' -> CharUtil.RIGHT_ARROW
                'D' -> CharUtil.LEFT_ARROW
                else -> inChar
            }
        }
        return inChar
    }

    /**
     * To see [jline2/issues/152](https://github.com/jline/jline2/issues/152)
     */
    fun processArrowBundle(inChar: Char, shell: Shell, reader: IConsoleReader): Char {
//        return if (inChar != CharUtil.ESCAPE) {
//            inChar
//        } else try {
//            val stream = reader.input as NonBlockingInputStream
//            // Value -2 is the special code meaning that stream reached its end
//            if (stream.peek(100) <= -2) {
//                return CharUtil.ESCAPE
//            }
//            var inner: Char
//            do {
//                inner = reader.readChar().toChar()
//            } while (inner == CharUtil.BRACKET_START)
//            when (inner) {
//                'A' -> CharUtil.UP_ARROW
//                'B' -> CharUtil.DOWN_ARROW
//                'C' -> CharUtil.RIGHT_ARROW
//                'D' -> CharUtil.LEFT_ARROW
//                else -> {
//                    shell.write(CharUtil.ESCAPE)
//                    shell.write(CharUtil.BRACKET_START)
//                    inner
//                }
//            }
//        } catch (e: Exception) {
//            inChar
//        }
        return inChar
    }

    fun processArrowBundle(inChar: Char, reader: IConsoleReader): Char {
//        return if (inChar != CharUtil.ESCAPE) {
//            inChar
//        } else try {
//            val stream = reader.input as NonBlockingInputStream
//            // Value -2 is the special code meaning that stream reached its end
//            if (stream.peek(100) <= -2) {
//                return CharUtil.ESCAPE
//            }
//            var inner: Char
//            do {
//                inner = reader.readChar().toChar()
//            } while (inner == CharUtil.BRACKET_START)
//            when (inner) {
//                'A' -> CharUtil.UP_ARROW
//                'B' -> CharUtil.DOWN_ARROW
//                'C' -> CharUtil.RIGHT_ARROW
//                'D' -> CharUtil.LEFT_ARROW
//                else -> inner
//            }
//        } catch (e: Exception) {
//            inChar
//        }
        return inChar
    }
}