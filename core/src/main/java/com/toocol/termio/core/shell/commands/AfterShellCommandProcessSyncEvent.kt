package com.toocol.termio.core.shell.commands

import com.toocol.termio.utilities.event.core.SyncEvent

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 20:25
 * @version: 0.0.1
 */
class AfterShellCommandProcessSyncEvent(
    val shellCommand: ShellCommand = ShellCommand.DEFAULT, val sessionId: Long = 0
) : SyncEvent()