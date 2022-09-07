package com.toocol.termio.core.shell.handlers

import com.toocol.termio.core.cache.*
import com.toocol.termio.core.cache.SshSessionCache.Instance.isDisconnect
import com.toocol.termio.core.shell.ShellAddress
import com.toocol.termio.core.shell.commands.ShellCommand
import com.toocol.termio.core.shell.commands.ShellCommand.Companion.cmdOf
import com.toocol.termio.core.shell.core.Shell
import com.toocol.termio.core.shell.core.ShellProtocol
import com.toocol.termio.core.term.TermAddress
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.core.term.core.TermStatus
import com.toocol.termio.utilities.execeptions.RemoteDisconnectException
import com.toocol.termio.utilities.module.BlockingMessageHandler
import com.toocol.termio.utilities.module.IAddress
import com.toocol.termio.utilities.sync.SharedCountdownLatch.await
import com.toocol.termio.utilities.utils.MessageBox
import com.toocol.termio.utilities.utils.StrUtil
import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import java.nio.charset.StandardCharsets
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 15:25
 */
class BlockingShellExecuteHandler(vertx: Vertx?, context: Context?, parallel: Boolean) : BlockingMessageHandler<Long?>(
    vertx!!, context!!, parallel) {

    private val shellCache = ShellCache.Instance

    override fun consume(): IAddress {
        return ShellAddress.RECEIVE_SHELL
    }

    @Throws(Exception::class)
    override fun <T> handleBlocking(promise: Promise<Long?>, message: Message<T>) {
        ACCEPT_SHELL_CMD_IS_RUNNING = true
        val sessionId = cast<Long>(message.body())
        val shell = shellCache.getShell(sessionId)
        if (shell == null) {
            promise.fail("Shell is null.")
            return
        }
        try {
            if (shell.protocol == ShellProtocol.SSH) {
                shell.writeAndFlush("export HISTCONTROL=ignoreboth\n".toByteArray(StandardCharsets.UTF_8))
            }
            while (true) {
                val cmdRead = shell.readCmd() ?: continue
                val cmd = StringBuilder(cmdRead)
                val isBreak = AtomicBoolean()
                val isContinue = AtomicBoolean()
                val completeSessionId = AtomicReference<Long?>()
                cmdOf(cmd.toString()).ifPresent { shellCommand: ShellCommand ->
                    try {
                        if (shell.getRemoteCmd().isNotEmpty()) {
                            return@ifPresent
                        }
                        val finalCmd: String?
                        val result = shellCommand.processCmd(eventBus, shell, isBreak, cmd.toString())
                        finalCmd = result._1()
                        completeSessionId.set(result._2())
                        cmd.delete(0, cmd.length)
                        if (finalCmd != null) {
                            cmd.append(finalCmd)
                        } else {
                            isContinue.set(true)
                        }
                    } catch (e: Exception) {
                        // do noting
                    }
                }
                cmdOf(shell.getRemoteCmd()).ifPresent { shellCommand: ShellCommand ->
                    try {
                        val finalCmd: String?
                        val result = shellCommand.processCmd(eventBus, shell, isBreak, shell.getRemoteCmd())
                        finalCmd = result._1()
                        completeSessionId.set(result._2())
                        if (finalCmd != null) {
                            cmd.append(finalCmd)
                        } else {
                            isContinue.set(true)
                        }
                    } catch (e: Exception) {
                        // do noting
                    }
                }
                shell.clearRemoteCmd()
                if (isBreak.get()) {
                    if (cmd.isNotEmpty()) {
                        shell.writeAndFlush(cmd.append("\t").toString().toByteArray(StandardCharsets.UTF_8))
                    }
                    promise.complete(completeSessionId.get())
                    break
                }
                if (isContinue.get()) {
                    continue
                }
                if (shell.status == Shell.Status.NORMAL) {
                    if (shell.protocol == ShellProtocol.SSH) {
                        shell.setLocalLastCmd(cmd.toString() + StrUtil.CRLF)
                    } else if (shell.protocol == ShellProtocol.MOSH) {
                        shell.setLocalLastCmd(cmd.toString() + StrUtil.LF)
                    }
                }
                if (isDisconnect(sessionId)) {
                    throw RemoteDisconnectException("Session disconnect.")
                }
                val latch = CountDownLatch(1)
                if (EXECUTE_CD_CMD) {
                    EXECUTE_CD_CMD = false
                    EXHIBIT_WAITING_BEFORE_COMMAND_PREPARE = true
                    val request = JsonObject()
                    request.put("sessionId", sessionId)
                    request.put("cmd", " pwd")
                    request.put("prefix", "/")
                    eventBus.request(ShellAddress.EXECUTE_SINGLE_COMMAND_IN_CERTAIN_SHELL.address(),
                        request) { result: AsyncResult<Message<Any?>> ->
                        shell.fullPath.set(cast(result.result().body()))
                        latch.countDown()
                        info("Current full path: {}", shell.fullPath.get())
                    }
                } else {
                    latch.countDown()
                }
                val actualCmd = cmd.toString().trim { it <= ' ' } + StrUtil.LF
                val remoteDisconnect = AtomicBoolean()
                await({
                    try {
                        shell.writeAndFlush(actualCmd.toByteArray(StandardCharsets.UTF_8))
                    } catch (e: RemoteDisconnectException) {
                        // do nothing
                        remoteDisconnect.set(true)
                    }
                }, 1000, this.javaClass, BlockingShellDisplayHandler::class.java)
                if (remoteDisconnect.get()) {
                    promise.tryComplete(sessionId)
                    break
                }
                if (isDisconnect(sessionId)) {
                    // check the session status before await
                    throw RemoteDisconnectException("Session disconnect.")
                }
                latch.await()
            }
        } catch (e: RemoteDisconnectException) {
            MessageBox.setErrorMessage(e.message)
            promise.tryComplete(sessionId)
        }
    }

    @Throws(Exception::class)
    override fun <T> resultBlocking(asyncResult: AsyncResult<Long?>, message: Message<T>) {
        ACCEPT_SHELL_CMD_IS_RUNNING = false
        Term.status = TermStatus.TERMIO
        val sessionId = asyncResult.result()
        if (HANGED_QUIT) {
            // hang up the session
            info("Hang up session, sessionId = {}", sessionId)
        } else if (SWITCH_SESSION) {
            info("Hang up this session waiting to switch, sessionId = {}", sessionId)
            SWITCH_SESSION = false
            SWITCH_SESSION_WAIT_HANG_PREVIOUS = true
            return
        } else {
            shellCache.stop(sessionId!!)
            info("Destroy session, sessionId = {}", sessionId)
        }
        eventBus.send(TermAddress.ACCEPT_COMMAND.address(), NORMAL_BACK)
    }
}