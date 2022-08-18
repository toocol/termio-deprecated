package com.toocol.termio.core.term.core

import com.toocol.termio.core.term.TermAddress
import com.toocol.termio.utilities.utils.CharUtil
import com.toocol.termio.utilities.utils.MessageBox
import com.toocol.termio.utilities.utils.StrUtil
import org.apache.commons.lang3.StringUtils
import kotlin.system.exitProcess

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/16 15:23
 */
class ConsoleTermReader(private val term: Term) : ITermReader {
    override fun readLine(): String {
        term.executeCursorOldX.set(term.cursorPosition[0])
        try {
            while (true) {
                val inChar = Term.reader.readCharacter().toChar()
                val finalChar: Char = term.escapeHelper.processArrowBundle(inChar, Term.reader)
                if (Term.status == TermStatus.HISTORY_OUTPUT && !CharUtil.isLeftOrRightArrow(finalChar) && finalChar != '\u001b') {
                    continue
                }
                if (term.termCharEventDispatcher.dispatch(term, finalChar)) {
                    val cmd: String = term.lineBuilder.toString()
                    term.lineBuilder.delete(0, term.lineBuilder.length)
                    if (StringUtils.isEmpty(cmd) && term.lastChar != CharUtil.CR) {
                        term.eventBus().send(TermAddress.TERMINAL_ECHO.address(), StrUtil.EMPTY)
                    }
                    term.lastChar = finalChar
                    term.eventBus().send(TermAddress.TERMINAL_ECHO_CLEAN_BUFFER.address(), StrUtil.EMPTY)
                    return cmd
                }
                if (Term.status == TermStatus.HISTORY_OUTPUT) {
                    continue
                }
                term.lastChar = finalChar
                term.printExecution(term.lineBuilder.toString())
                term.eventBus().send(TermAddress.TERMINAL_ECHO.address(), term.lineBuilder.toString())
            }
        } catch (e: Exception) {
            MessageBox.setExitMessage("Term reader error.")
            exitProcess(-1)
        }
    }
}