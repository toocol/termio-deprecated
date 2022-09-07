package com.toocol.termio.core.shell.commands.processors

import com.toocol.termio.core.cache.SWITCH_SESSION
import com.toocol.termio.core.shell.commands.ShellCommandProcessor
import com.toocol.termio.core.shell.core.Shell
import com.toocol.termio.utilities.utils.Tuple2
import io.vertx.core.eventbus.EventBus
import java.util.concurrent.atomic.AtomicBoolean

/**
 * command: lc
 * desc:    list all the connection properties to quick switch
 *
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/7/28 14:32
 */
class ShellLcCmdProcessor : ShellCommandProcessor() {
    override fun process(
        eventBus: EventBus,
        shell: Shell,
        isBreak: AtomicBoolean,
        cmd: String
    ): Tuple2<String?, Long?> {
        val changeSession = shell.switchSession()
        isBreak.set(changeSession)
        SWITCH_SESSION = changeSession
        return Tuple2(null, shell.sessionId)
    }
}