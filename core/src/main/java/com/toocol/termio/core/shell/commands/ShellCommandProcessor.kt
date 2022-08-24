package com.toocol.termio.core.shell.commands

import com.toocol.termio.core.shell.core.Shell
import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.Tuple2
import io.vertx.core.eventbus.EventBus
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/2 17:42
 */
abstract class ShellCommandProcessor : Loggable{

    /**
     * process the shell command
     *
     * @param eventBus event bus
     * @param shell    shell
     * @param isBreak  break the shell accept cycle
     * @return final cmd should be executed
     */
    abstract fun process(eventBus: EventBus, shell: Shell, isBreak: AtomicBoolean, cmd: String)
            : Tuple2<String?, Long?>

    fun processInner(eventBus: EventBus, shell: Shell, isBreak: AtomicBoolean, cmd: String, shellCommand: ShellCommand): Tuple2<String?, Long?>{
        BeforeShellCommandProcessSyncEvent(shellCommand, cmd, shell.sessionId).dispatch()

        val ret = process(eventBus, shell, isBreak, cmd)

        AfterShellCommandProcessSyncEvent(shellCommand, shell.sessionId).dispatch()
        return ret
    }

}