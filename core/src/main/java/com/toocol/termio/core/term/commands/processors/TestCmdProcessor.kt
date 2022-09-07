package com.toocol.termio.core.term.commands.processors

import com.toocol.termio.core.term.commands.TermCommandProcessor
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.utilities.utils.Tuple2
import io.vertx.core.eventbus.EventBus

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/7/12 1:18
 * @version: 0.0.1
 */
class TestCmdProcessor : TermCommandProcessor() {
    override fun process(eventBus: EventBus, cmd: String, resultAndMsg: Tuple2<Boolean, String?>): Any? {
        Term.instance.printTest()
        resultAndMsg.first(true)
        return null
    }
}