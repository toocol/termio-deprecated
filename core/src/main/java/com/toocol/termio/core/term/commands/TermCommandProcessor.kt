package com.toocol.termio.core.term.commands

import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.Tuple2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/2 17:32
 */
abstract class TermCommandProcessor : Loggable, CoroutineScope by MainScope() {
    /**
     * process command
     *
     * @param cmd          cmd
     * @param resultAndMsg resultAndMsg
     */
    abstract fun process(cmd: String, resultAndMsg: Tuple2<Boolean, String?>): Any?

    fun processInner(cmd: String, resultAndMsg: Tuple2<Boolean, String?>, termCommand: TermCommand) {
        BeforeTermCommandProcessSyncEvent(termCommand, cmd).dispatch()

        val ret = process(cmd, resultAndMsg)

        AfterTermCommandProcessSyncEvent(termCommand, resultAndMsg._1(), ret).dispatch()
    }
}