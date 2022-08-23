package com.toocol.termio.core.term.commands

import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.Tuple2
import io.vertx.core.eventbus.EventBus

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/2 17:32
 */
abstract class TermCommandProcessor : Loggable {
    /**
     * process command
     *
     * @param eventBus     event bus
     * @param cmd          cmd
     * @param resultAndMsg resultAndMsg
     */
    abstract fun process(eventBus: EventBus, cmd: String, resultAndMsg: Tuple2<Boolean, String?>): Any?

    fun processInner(eventBus: EventBus, cmd: String, resultAndMsg: Tuple2<Boolean, String?>) {
        BeforeTermCommandProcessSyncEvent(cmd).dispatch()

        val ret = process(eventBus, cmd, resultAndMsg)

        AfterTermCommandProcessSyncEvent(resultAndMsg._1(), ret).dispatch()
    }
}