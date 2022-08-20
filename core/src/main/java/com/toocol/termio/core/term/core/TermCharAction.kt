package com.toocol.termio.core.term.core

import com.toocol.termio.core.Termio.Companion.runType
import com.toocol.termio.utilities.action.AbstractCharAction
import com.toocol.termio.utilities.event.CharEvent
import com.toocol.termio.core.Termio

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:13
 */
abstract class TermCharAction : AbstractCharAction<Term>() {
    override fun act(device: Term, charEvent: CharEvent, inChar: Char): Boolean {
        return if (runType() == Termio.RunType.CONSOLE) actOnConsole(device, charEvent, inChar)
        else actOnDesktop(device, charEvent, inChar)
    }

    abstract fun actOnConsole(term: Term, charEvent: CharEvent, inChar: Char): Boolean
    abstract fun actOnDesktop(term: Term, charEvent: CharEvent, inChar: Char): Boolean
}