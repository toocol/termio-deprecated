package com.toocol.termio.core.term.core

import com.toocol.termio.utilities.utils.MessageBox
import kotlin.system.exitProcess

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/16 15:23
 */
class DesktopTermReader(private val term: Term) : ITermReader {
    override fun readLine(): String {
        try {
            while (true) {
                val inChar = Term.reader!!.readCharacter().toChar()
                val finalChar: Char = term.escapeHelper.processArrowBundle(inChar, Term.reader!!)
                if (term.termCharEventDispatcher.dispatch(term, finalChar)) {
                    val cmd: String = term.lineBuilder.toString()
                    term.lineBuilder.delete(0, term.lineBuilder.length)
                    term.lastChar = finalChar
                    return cmd
                }
                term.lastChar = finalChar
            }
        } catch (e: Exception) {
            MessageBox.setExitMessage("Term reader error.")
            exitProcess(-1)
        }
    }
}