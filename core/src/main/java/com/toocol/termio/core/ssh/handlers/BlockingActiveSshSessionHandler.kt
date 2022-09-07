package com.toocol.termio.core.ssh.handlers

import com.jcraft.jsch.ChannelShell
import com.toocol.termio.core.Termio
import com.toocol.termio.core.cache.CredentialCache
import com.toocol.termio.core.cache.ShellCache
import com.toocol.termio.core.cache.SshSessionCache
import com.toocol.termio.core.shell.core.Shell
import com.toocol.termio.core.shell.core.ShellProtocol
import com.toocol.termio.core.ssh.SshAddress
import com.toocol.termio.core.ssh.core.SshSessionFactory
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.utilities.ansi.AnsiStringBuilder
import com.toocol.termio.utilities.functional.Executable
import com.toocol.termio.utilities.functional.Ordered
import com.toocol.termio.utilities.module.BlockingMessageHandler
import com.toocol.termio.utilities.module.IAddress
import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * Active an ssh session without enter the Shell.
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 20:49
 * @version: 0.0.1
 */
@Ordered
class BlockingActiveSshSessionHandler(vertx: Vertx?, context: Context?, parallel: Boolean) :
    BlockingMessageHandler<JsonObject?>(
        vertx!!, context!!, parallel) {

    private val credentialCache = CredentialCache.Instance
    private val shellCache = ShellCache.Instance
    private val sshSessionCache = SshSessionCache.Instance
    private val factory = SshSessionFactory.Instance

    @Throws(Exception::class)
    override fun <T> handleBlocking(promise: Promise<JsonObject?>, message: Message<T>) {
        val ret = JsonObject()
        val success = JsonArray()
        val failed = JsonArray()
        val index = cast<JsonArray>(message.body())
        val rec = AtomicInteger()
        for (i in 0 until index.size()) {
            val credential = credentialCache.getCredential(index.getInteger(i))!!
            try {
                val sessionId = AtomicReference(sshSessionCache.containSession(
                    credential.host))
                val execute = Executable {
                    Optional.ofNullable(sshSessionCache.getChannelShell(sessionId.get())).ifPresent { channelShell: ChannelShell ->
                        val width = Termio.windowWidth
                        val height = Termio.windowHeight
                        channelShell.setPtySize(width, height, width, height)
                    }
                    System.gc()
                    if (sessionId.get() > 0) {
                        success.add(credential.host + "@" + credential.user)
                    } else {
                        failed.add(credential.host + "@" + credential.user)
                    }
                    if (rec.incrementAndGet() == index.size()) {
                        shellCache.initializeQuickSessionSwitchHelper()
                        ret.put("success", success)
                        ret.put("failed", failed)
                        promise.complete(ret)
                    }
                }
                if (sessionId.get() == 0L) {
                    sessionId.set(factory.createSession(credential))
                    val shell = Shell(sessionId.get(),
                        credential.host,
                        credential.user,
                        vertx,
                        eventBus,
                        sshSessionCache.getChannelShell(sessionId.get()))
                    shell.user = credential.user
                    shellCache.putShell(sessionId.get(), shell)
                    shell.initialFirstCorrespondence(ShellProtocol.SSH, execute)
                } else {
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
                        shell.initialFirstCorrespondence(ShellProtocol.SSH, execute)
                    } else {
                        val shell = shellCache.getShell(sessionId.get()) ?: throw RuntimeException()
                        shell.resetIO(ShellProtocol.SSH)
                        sessionId.set(newSessionId)
                        execute.execute()
                    }
                }
            } catch (e: Exception) {
                failed.add(credential.host + "@" + credential.user)
                if (rec.incrementAndGet() == index.size()) {
                    ret.put("success", success)
                    ret.put("failed", failed)
                    promise.complete(ret)
                }
            }
        }
    }

    @Throws(Exception::class)
    override fun <T> resultBlocking(asyncResult: AsyncResult<JsonObject?>, message: Message<T>) {
        if (asyncResult.succeeded()) {
            val term = Term.instance
            term.printScene(false)
            val activeMsg = asyncResult.result()!!
            val ansiStringBuilder = AnsiStringBuilder()
            val width = Termio.windowWidth
            for ((key, value1) in activeMsg) {
                if ("success" == key) {
                    ansiStringBuilder.append("$key:")
                    val value = value1.toString()
                    val split = value.replace("[", "").replace("]", "").replace("\"", "").split(",").toTypedArray()
                    for (i in split.indices) {
                        if (width < 24 * 3) {
                            if (i != 0 && i % 2 == 0) {
                                ansiStringBuilder.append("\n")
                            }
                        } else {
                            if (i != 0 && i % 3 == 0) {
                                ansiStringBuilder.append("\n")
                            }
                        }
                        ansiStringBuilder.front(Term.theme.activeSuccessMsgColor.color)
                            .background(Term.theme.displayBackGroundColor.color)
                            .append(split[i] + StringUtils.repeat(" ", 4))
                    }
                } else {
                    ansiStringBuilder.deFront().append("$key:")
                    val value = value1.toString()
                    val split = value.replace("[", "").replace("]", "").replace("\"", "").split(",").toTypedArray()
                    for (j in split.indices) {
                        if (width < 24 * 3) {
                            if (j != 0 && j % 2 == 0) {
                                ansiStringBuilder.append("\n")
                            }
                        } else {
                            if (j != 0 && j % 3 == 0) {
                                ansiStringBuilder.append("\n")
                            }
                        }
                        ansiStringBuilder.front(Term.theme.activeFailedMsgColor.color)
                            .background(Term.theme.displayBackGroundColor.color)
                            .append(split[j] + StringUtils.repeat(" ", 4))
                    }
                }
            }
            term.printDisplay(ansiStringBuilder.toString())
            message.reply(true)
        } else {
            message.reply(false)
        }
    }

    override fun consume(): IAddress {
        return SshAddress.ACTIVE_SSH_SESSION
    }
}