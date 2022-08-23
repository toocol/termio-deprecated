package com.toocol.termio.core.term.commands

import com.toocol.termio.utilities.event.core.SyncEvent

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 18:28
 * @version: 0.0.1
 */
class AfterTermCommandProcessSyncEvent(
    val termCommand: TermCommand = TermCommand.DEFAULT, val success: Boolean = false, val param: Any? = Any()
) : SyncEvent()