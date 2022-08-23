package com.toocol.termio.core.term.commands

import com.toocol.termio.utilities.event.core.SyncEvent
import com.toocol.termio.utilities.utils.StrUtil

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 18:26
 * @version: 0.0.1
 */
class BeforeTermCommandProcessSyncEvent(
    val termCommand: TermCommand = TermCommand.DEFAULT, val cmd: String = StrUtil.EMPTY
) : SyncEvent()
