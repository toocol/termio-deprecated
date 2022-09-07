package com.toocol.termio.core.shell.handlers

import com.toocol.termio.core.cache.ACCESS_EXHIBIT_SHELL_WITH_PROMPT
import com.toocol.termio.core.cache.EXHIBIT_WAITING_BEFORE_COMMAND_PREPARE
import com.toocol.termio.core.cache.JUST_CLOSE_EXHIBIT_SHELL
import com.toocol.termio.core.cache.ShellCache
import com.toocol.termio.core.shell.ShellAddress
import com.toocol.termio.core.shell.core.CmdFeedbackHelper
import com.toocol.termio.core.shell.core.ShellProtocol
import com.toocol.termio.utilities.module.BlockingMessageHandler
import com.toocol.termio.utilities.module.IAddress
import com.toocol.termio.utilities.sync.SharedCountdownLatch.await
import com.toocol.termio.utilities.utils.StrUtil
import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import java.nio.charset.StandardCharsets

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/10 22:45
 * @version: 0.0.1
 */
class BlockingExecuteCmdInShellHandler(vertx: Vertx?, context: Context?, parallel: Boolean) :
    BlockingMessageHandler<String?>(
        vertx!!, context!!, parallel) {
    private val shellCache = ShellCache.Instance

    override fun consume(): IAddress {
        return ShellAddress.EXECUTE_SINGLE_COMMAND_IN_CERTAIN_SHELL
    }

    @Throws(Exception::class)
    override fun <T> handleBlocking(promise: Promise<String?>, message: Message<T>) {
        val request = cast<JsonObject>(message.body())
        val sessionId = request.getLong("sessionId")
        val cmd = request.getString("cmd")
        val prefix = request.getString("prefix")
        await(
            {
                JUST_CLOSE_EXHIBIT_SHELL = true
                EXHIBIT_WAITING_BEFORE_COMMAND_PREPARE = false
            },
            this.javaClass,
            BlockingShellDisplayHandler::class.java
        )
        val shell = shellCache.getShell(sessionId)
        if (shell == null) {
            promise.complete("/")
            return
        }
        val inputStream = shell.getInputStream(ShellProtocol.SSH)
        val outputStream = shell.getOutputStream(ShellProtocol.SSH)
        if (outputStream == null) {
            promise.complete("/")
            return
        }
        outputStream.write((cmd + StrUtil.LF).toByteArray(StandardCharsets.UTF_8))
        outputStream.flush()
        val feedback = CmdFeedbackHelper(inputStream, cmd, shell, prefix).extractFeedback()
        ACCESS_EXHIBIT_SHELL_WITH_PROMPT = false
        eventBus.send(ShellAddress.DISPLAY_SHELL.address(), sessionId)
        promise.complete(feedback)
    }

    @Throws(Exception::class)
    override fun <T> resultBlocking(asyncResult: AsyncResult<String?>, message: Message<T>) {
        if (asyncResult.succeeded()) {
            message.reply(asyncResult.result())
        } else {
            message.reply(null)
        }
    }
}