package com.toocol.termio.core.term.commands.processors

import com.toocol.termio.core.term.commands.TermCommandProcessor
import com.toocol.termio.core.term.core.HistoryOutputInfoHelper.Companion.instance
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.core.term.core.TermStatus
import com.toocol.termio.utilities.utils.Tuple2
import io.vertx.core.eventbus.EventBus

class HistoryCmdProcessor : TermCommandProcessor() {
    private val historyOutputInfoHelper = instance
    override fun process(eventBus: EventBus, cmd: String, resultAndMsg: Tuple2<Boolean, String?>): Any? {
        Term.status = TermStatus.HISTORY_OUTPUT
        historyOutputInfoHelper.displayInformation()
        resultAndMsg.first(true)
        return null
    }
}