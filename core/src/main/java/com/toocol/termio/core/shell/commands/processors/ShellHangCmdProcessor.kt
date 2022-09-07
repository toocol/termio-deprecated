package com.toocol.termio.core.shell.commands.processors

import com.toocol.termio.core.cache.HANGED_QUIT
import com.toocol.termio.core.shell.commands.ShellCommandProcessor
import com.toocol.termio.core.shell.core.Shell
import com.toocol.termio.utilities.utils.StrUtil
import com.toocol.termio.utilities.utils.Tuple2
import io.vertx.core.eventbus.EventBus
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 13:52
 */
class ShellHangCmdProcessor : ShellCommandProcessor() {
    override fun process(
        eventBus: EventBus,
        shell: Shell,
        isBreak: AtomicBoolean,
        cmd: String
    ): Tuple2<String?, Long?> {
        shell.cleanUp()
        isBreak.set(true)
        HANGED_QUIT = true
        return Tuple2(StrUtil.EMPTY, shell.sessionId)
    }
}