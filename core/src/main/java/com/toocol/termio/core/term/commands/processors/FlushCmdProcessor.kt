package com.toocol.termio.core.term.commands.processors

import com.toocol.termio.core.Termio
import com.toocol.termio.core.term.commands.TermCommandProcessor
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.core.term.core.Term.Companion.promptLen
import com.toocol.termio.utilities.ansi.AnsiStringBuilder
import com.toocol.termio.utilities.ansi.Printer.clear
import com.toocol.termio.utilities.ansi.Printer.print
import com.toocol.termio.utilities.utils.Tuple2
import io.vertx.core.eventbus.EventBus

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 19:24
 */
class FlushCmdProcessor : TermCommandProcessor() {
    override fun process(eventBus: EventBus, cmd: String, resultAndMsg: Tuple2<Boolean, String?>): Any? {
        clear()
        val term = Term.instance
        term.printScene(false)
        term.setCursorPosition(Term.leftMargin, Term.executeLine)
        val builder = AnsiStringBuilder()
            .background(Term.theme.executeBackgroundColor.color)
            .front(Term.theme.executeFrontColor.color)
            .append(Term.prompt)
            .append(" ".repeat(Termio.windowWidth - promptLen - Term.leftMargin))
        print(builder.toString())
        term.cleanDisplayBuffer()
        resultAndMsg.first(true)
        return null
    }
}