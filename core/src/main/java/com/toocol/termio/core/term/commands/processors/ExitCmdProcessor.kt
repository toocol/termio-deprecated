package com.toocol.termio.core.term.commands.processors

import com.toocol.termio.core.term.commands.TermCommandProcessor
import com.toocol.termio.utilities.utils.Tuple2
import io.vertx.core.eventbus.EventBus
import kotlin.system.exitProcess

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 14:53
 */
class ExitCmdProcessor : TermCommandProcessor() {
    override fun process(eventBus: EventBus, cmd: String, resultAndMsg: Tuple2<Boolean, String?>): Any? {
        exitProcess(-1)
    }
}