package com.toocol.termio.core.ssh.handlers

import com.jcraft.jsch.ChannelShell
import com.toocol.termio.core.Termio
import com.toocol.termio.core.cache.*
import com.toocol.termio.core.shell.ShellAddress
import com.toocol.termio.core.shell.core.Shell
import com.toocol.termio.core.shell.core.ShellProtocol
import com.toocol.termio.core.ssh.SshAddress
import com.toocol.termio.core.ssh.core.SshSessionFactory
import com.toocol.termio.core.term.TermAddress
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.core.term.core.TermStatus
import com.toocol.termio.utilities.functional.Executable
import com.toocol.termio.utilities.module.BlockingMessageHandler
import com.toocol.termio.utilities.module.IAddress
import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import java.util.*
import java.util.concurrent.atomic.AtomicReference

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
abstract class AbstractBlockingEstablishSshSessionHandler(vertx: Vertx?, context: Context?, parallel: Boolean) :
    BlockingMessageHandler<Long?>(
        vertx!!, context!!, parallel) {

    protected val credentialCache = CredentialCache.Instance
    protected val sshSessionCache = SshSessionCache.Instance
    protected val shellCache = ShellCache.Instance
    protected val factory = SshSessionFactory.Instance

    override fun consume(): IAddress {
        return SshAddress.ESTABLISH_SSH_SESSION
    }

    @Throws(Exception::class)
    override fun <T> handleBlocking(promise: Promise<Long?>, message: Message<T>) {
        val index = cast<Int>(message.body())
        val credential = credentialCache.getCredential(index)
        try {
            assert(credential != null)
            val sessionId = AtomicReference(sshSessionCache.containSession(
                credential!!.host))

            // execute in the final
            val execute = Executable {
                Optional.ofNullable(sshSessionCache.getChannelShell(sessionId.get())).ifPresent { channelShell: ChannelShell ->
                    val width = Termio.windowWidth
                    val height = Termio.windowHeight
                    channelShell.setPtySize(width, height, width, height)
                }
                HANGED_QUIT = false
                shellCache.initializeQuickSessionSwitchHelper()
                if (sessionId.get() > 0) {
                    promise.complete(sessionId.get())
                } else {
                    promise.fail("Session establish failed.")
                }
            }
            if (sessionId.get() == 0L) {
                HANGED_ENTER = false
                sessionId.set(factory.createSession(credential))
                val shell = Shell(sessionId.get(),
                    credential.host,
                    credential.user,
                    vertx,
                    eventBus,
                    sshSessionCache.getChannelShell(sessionId.get()))
                shell.user = credential.user
                shellCache.putShell(sessionId.get(), shell)
                shell.setJumpServer(credential.isJumpServer)
                shell.initialFirstCorrespondence(ShellProtocol.SSH, execute)
            } else {
                HANGED_ENTER = true
                val newSessionId = factory.invokeSession(sessionId.get(), credential)
                if (newSessionId != sessionId.get() || !shellCache.contains(newSessionId)) {
                    val shell = Shell(sessionId.get(),
                        credential.host,
                        credential.user,
                        vertx,
                        eventBus,
                        sshSessionCache.getChannelShell(sessionId.get()))
                    shell.user = credential.user
                    shellCache.putShell(sessionId.get(), shell)
                    sessionId.set(newSessionId)
                    shell.setJumpServer(credential.isJumpServer)
                    shell.initialFirstCorrespondence(ShellProtocol.SSH, execute)
                } else {
                    val shell = shellCache.getShell(newSessionId)
                    if (shell == null) {
                        promise.complete(null)
                        return
                    }
                    if (shell.channelShell == null) {
                        /* If the connection is established through Mosh, it needs to be set the ChannelShell*/
                        shell.channelShell = sshSessionCache.getChannelShell(sessionId.get())
                    }
                    shell.resetIO(ShellProtocol.SSH)
                    sessionId.set(newSessionId)
                    execute.execute()
                }
            }
        } catch (e: Exception) {
            promise.complete(null)
        } finally {
            // invoke gc() to clean up already un-use object during initial processing. (it's very efficacious :))
            System.gc()
        }
    }

    @Throws(Exception::class)
    override fun <T> resultBlocking(asyncResult: AsyncResult<Long?>, message: Message<T>) {
        val sessionId = asyncResult.result()
        if (sessionId != null) {
            val shell = shellCache.getShell(sessionId)
            if (shell == null) {
                warn("Get Shell is null when try to entry shell.")
                eventBus.send(TermAddress.ACCEPT_COMMAND.address(), CONNECT_FAILED)
                return
            }
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