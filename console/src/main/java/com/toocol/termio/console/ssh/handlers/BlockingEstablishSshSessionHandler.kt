package com.toocol.termio.console.ssh.handlers

import com.toocol.termio.core.cache.CONNECT_FAILED
import com.toocol.termio.core.cache.MONITOR_SESSION_ID
import com.toocol.termio.core.cache.SHOW_WELCOME
import com.toocol.termio.core.cache.ShellCache.Instance.getShell
import com.toocol.termio.core.shell.ShellAddress
import com.toocol.termio.core.ssh.handlers.AbstractBlockingEstablishSshSessionHandler
import com.toocol.termio.core.term.TermAddress
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.core.term.core.TermStatus
import com.toocol.termio.utilities.console.Console.Companion.get
import com.toocol.termio.utilities.functional.Ordered
import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
@Ordered
class BlockingEstablishSshSessionHandler(vertx: Vertx?, context: Context?, parallel: Boolean) :
    AbstractBlockingEstablishSshSessionHandler(vertx, context, parallel) {
    @Throws(Exception::class)
    override fun <T> resultBlocking(asyncResult: AsyncResult<Long>, message: Message<T>) {
        val sessionId = asyncResult.result()
        if (sessionId != null) {
            val shell = getShell(sessionId)
            if (shell == null) {
                warn("Get Shell is null when try to entry shell.")
                eventBus.send(TermAddress.ACCEPT_COMMAND.address(), CONNECT_FAILED)
                return
            }
            shell.registerConsole(get())
            shell.printAfterEstablish()
            SHOW_WELCOME = true
            MONITOR_SESSION_ID = sessionId
            Term.status = TermStatus.SHELL
            eventBus.send(ShellAddress.DISPLAY_SHELL.address(), sessionId)
            eventBus.send(ShellAddress.RECEIVE_SHELL.address(), sessionId)
        } else {
            warn("Establish ssh connection failed.")
            eventBus.send(TermAddress.ACCEPT_COMMAND.address(), CONNECT_FAILED)
        }
    }
}