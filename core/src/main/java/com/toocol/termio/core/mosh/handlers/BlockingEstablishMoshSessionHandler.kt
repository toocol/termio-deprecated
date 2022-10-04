package com.toocol.termio.core.mosh.handlers

import com.toocol.termio.core.cache.*
import com.toocol.termio.core.mosh.MoshAddress
import com.toocol.termio.core.mosh.core.MoshSessionFactory.Companion.factory
import com.toocol.termio.core.shell.ShellAddress
import com.toocol.termio.core.shell.core.Shell
import com.toocol.termio.core.shell.core.ShellProtocol
import com.toocol.termio.core.term.TermAddress
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.core.term.core.TermStatus
import com.toocol.termio.utilities.ansi.Printer.clear
import com.toocol.termio.utilities.functional.Ordered
import com.toocol.termio.utilities.module.BlockingMessageHandler
import com.toocol.termio.utilities.module.IAddress
import com.toocol.termio.utilities.utils.MessageBox
import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/28 23:44
 * @version: 0.0.1
 */
@Ordered
class BlockingEstablishMoshSessionHandler(vertx: Vertx?, context: Context?, parallel: Boolean) :
    BlockingMessageHandler<Long?>(
        vertx!!, context!!, parallel) {
    private val sshSessionCache = SshSessionCache.Instance
    private val credentialCache = CredentialCache
    private val shellCache = ShellCache.Instance
    private val moshSessionFactory = factory(vertx!!)

    @Throws(Exception::class)
    override fun <T> handleBlocking(promise: Promise<Long?>, message: Message<T>) {
        val index = cast<Int>(message.body())
        val credential = credentialCache.getCredential(index)
        if (credential == null) {
            promise.fail("Credential not exist.")
            return
        }
        val session = moshSessionFactory!!.getSession(credential)
        if (session == null) {
            promise.fail("Can't touch the mosh-server.")
            return
        }
        val sessionId = session.sessionId

        // let event loop thread pool to handler udp packet receive.
        eventBus.request(MoshAddress.LISTEN_LOCAL_SOCKET.address(), sessionId) { result: AsyncResult<Message<Any?>?> ->
            if (result.succeeded()) {
                try {
                    eventBus.send(MoshAddress.MOSH_TICK.address(), sessionId)
                    val shell = Shell(sessionId, session)
                    shell.user = credential.user
                    shell.initialFirstCorrespondence(ShellProtocol.MOSH)

                    shellCache.putShell(sessionId, shell)
                    shell.channelShell = sshSessionCache.getChannelShell(sessionId)
                    shellCache.initializeQuickSessionSwitchHelper()
                    clear()
                    SHOW_WELCOME = true
                    HANGED_QUIT = false
                    MONITOR_SESSION_ID = sessionId
                    Term.status = TermStatus.SHELL
                    eventBus.send(ShellAddress.DISPLAY_SHELL.address(), sessionId)
                    eventBus.send(ShellAddress.RECEIVE_SHELL.address(), sessionId)
                    System.gc()
                } catch (e: Exception) {
                    eventBus.send(TermAddress.ACCEPT_COMMAND.address(), CONNECT_FAILED)
                }
            } else {
                eventBus.send(TermAddress.ACCEPT_COMMAND.address(), CONNECT_FAILED)
            }
        }
        promise.complete()
    }

    @Throws(Exception::class)
    override fun <T> resultBlocking(asyncResult: AsyncResult<Long?>, message: Message<T>) {
        if (!asyncResult.succeeded()) {
            warn("Establish mosh connection failed.")
            MessageBox.setErrorMessage("Can't touch the mosh-server.")
            eventBus.send(TermAddress.ACCEPT_COMMAND.address(), NORMAL_BACK)
        }
    }

    override fun consume(): IAddress {
        return MoshAddress.ESTABLISH_MOSH_SESSION
    }
}