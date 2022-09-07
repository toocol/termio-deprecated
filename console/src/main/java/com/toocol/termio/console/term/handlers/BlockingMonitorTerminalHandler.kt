package com.toocol.termio.console.term.handlers

import com.toocol.termio.core.Termio
import com.toocol.termio.core.cache.MONITOR_SESSION_ID
import com.toocol.termio.core.cache.STOP_PROGRAM
import com.toocol.termio.core.cache.ShellCache
import com.toocol.termio.core.cache.SshSessionCache
import com.toocol.termio.core.ssh.core.SshSession
import com.toocol.termio.core.term.TermAddress
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.core.term.core.TermStatus
import com.toocol.termio.utilities.console.Console.Companion.get
import com.toocol.termio.utilities.module.BlockingMessageHandler
import com.toocol.termio.utilities.module.IAddress
import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/13 0:58
 * @version: 0.0.1
 */
class BlockingMonitorTerminalHandler(vertx: Vertx?, context: Context?, parallel: Boolean) :
    BlockingMessageHandler<Void?>(
        vertx!!, context!!, parallel) {
    private val console = get()
    private val shellCache = ShellCache.Instance
    private val sshSessionCache = SshSessionCache.Instance

    override fun consume(): IAddress {
        return TermAddress.MONITOR_TERMINAL
    }

    @Throws(Exception::class)
    override fun <T> handleBlocking(promise: Promise<Void?>, message: Message<T>) {
        Termio.windowWidth = console.getWindowWidth()
        Termio.windowHeight = console.getWindowHeight()
        while (true) {
            monitorTerminalSize()
            monitorSshSession()
            if (STOP_PROGRAM) {
                break
            }
            Thread.sleep(100)
        }
        promise.complete()
    }

    @Throws(Exception::class)
    override fun <T> resultBlocking(asyncResult: AsyncResult<Void?>, message: Message<T>) {
    }

    private fun monitorTerminalSize() {
        val terminalWidth = console.getWindowWidth()
        val terminalHeight = console.getWindowHeight()
        if (terminalWidth < 0 || terminalHeight < 0) {
            return
        }
        if (Termio.windowWidth != terminalWidth || Termio.windowHeight != terminalHeight) {
            Termio.windowWidth = terminalWidth
            Termio.windowHeight = terminalHeight
            if (Term.status == TermStatus.SHELL) {
                shellCache.getShell(MONITOR_SESSION_ID)!!
                    .resize(terminalWidth, terminalHeight, MONITOR_SESSION_ID)
            } else if (Term.status == TermStatus.TERMIO) {
                Term.instance.printScene(true)
            }
        }
    }

    private fun monitorSshSession() {
        sshSessionCache.sessionMap.forEach { (sessionId: Long?, session: SshSession?) ->
            if (!session.alive()) {
                if (!sshSessionCache.containSessionId(sessionId)) {
                    return@forEach
                }
                sshSessionCache.stop(sessionId)
                shellCache.initializeQuickSessionSwitchHelper()
            }
        }
    }
}