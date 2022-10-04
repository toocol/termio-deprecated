package com.toocol.termio.core.term.commands.processors

import com.toocol.termio.core.term.commands.TermCommandProcessor
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.utilities.utils.Tuple2

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 21:14
 * @version: 0.0.1
 */
class HelloCmdProcessor : TermCommandProcessor() {
    override fun process(cmd: String, resultAndMsg: Tuple2<Boolean, String?>): Any? {
        Term.instance.printDisplay("Hello you ~")
        resultAndMsg.first(true)
        return null
    }
}