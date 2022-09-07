package com.toocol.termio.core.shell.commands.processors

import com.toocol.termio.core.shell.commands.ShellCommandProcessor
import com.toocol.termio.core.shell.core.Shell
import com.toocol.termio.utilities.utils.Tuple2
import io.vertx.core.eventbus.EventBus
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 11:47
 */
class ShellExitCmdProcessor : ShellCommandProcessor() {
    override fun process(
        eventBus: EventBus,
        shell: Shell,
        isBreak: AtomicBoolean,
        cmd: String
    ): Tuple2<String?, Long?> {
        isBreak.set(true)
        return Tuple2("exit", shell.sessionId)
    }
}