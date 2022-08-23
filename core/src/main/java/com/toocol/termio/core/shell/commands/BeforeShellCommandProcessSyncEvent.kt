package com.toocol.termio.core.shell.commands

import com.toocol.termio.utilities.event.core.SyncEvent
import com.toocol.termio.utilities.utils.StrUtil

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 20:23
 * @version: 0.0.1
 */
class BeforeShellCommandProcessSyncEvent(
    shellCommand: ShellCommand = ShellCommand.DEFAULT, val cmd: String = StrUtil.EMPTY, val sessionId: Long = 0
) : SyncEvent()