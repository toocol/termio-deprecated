package com.toocol.termio.core.shell.commands.processors

import com.toocol.termio.core.shell.ShellAddress
import com.toocol.termio.core.shell.commands.ShellCommandProcessor
import com.toocol.termio.core.shell.core.Shell
import com.toocol.termio.utilities.ansi.Printer.println
import com.toocol.termio.utilities.utils.StrUtil
import com.toocol.termio.utilities.utils.Tuple2
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/9 16:33
 * @version: 0.0.1
 */
class ShellUfCmdProcessor : ShellCommandProcessor() {
    override fun process(
        eventBus: EventBus,
        shell: Shell,
        isBreak: AtomicBoolean,
        cmd: String
    ): Tuple2<String?, Long?> {
        if (cmd.contains(StrUtil.SPACE)) {
            val ignore = cmd.replace("uf ".toRegex(), "")
            println("uf: should have no params, ignored '" + ignore.trim { it <= ' ' } + "'.")
        }
        val remotePath = shell.fullPath.get()
        val request = JsonObject()
        request.put("sessionId", shell.sessionId)
        request.put("remotePath", remotePath)
        eventBus.send(ShellAddress.START_UF_COMMAND.address(), request)
        return Tuple2(null, null)
    }
}