package com.toocol.termio.core.shell.handlers

import com.toocol.termio.core.cache.ShellCache
import com.toocol.termio.core.shell.ShellAddress
import com.toocol.termio.core.shell.core.CmdFeedbackHelper
import com.toocol.termio.core.shell.core.ExecChannelProvider
import com.toocol.termio.utilities.module.BlockingMessageHandler
import com.toocol.termio.utilities.module.IAddress
import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/10 17:57
 * @version: 0.0.1
 */
class BlockingExecuteSingleCmdHandler(vertx: Vertx?, context: Context?, parallel: Boolean) :
    BlockingMessageHandler<String?>(
        vertx!!, context!!, parallel) {

    private val shellCache = ShellCache.Instance
    private val execChannelProvider = ExecChannelProvider.Instance

    override fun consume(): IAddress {
        return ShellAddress.EXECUTE_SINGLE_COMMAND
    }

    @Throws(Exception::class)
    override fun <T> handleBlocking(promise: Promise<String?>, message: Message<T>) {
        val request = cast<JsonObject>(message.body())
        val sessionId = request.getLong("sessionId")
        val cmd = request.getString("cmd")
        val prefix = request.getString("prefix")
        val channelExec = execChannelProvider.getChannelExec(sessionId)
        val shell = shellCache.getShell(sessionId)
        if (shell == null) {
            promise.fail("ChannelExec or shell is null.")
            return
        }
        val inputStream = channelExec.inputStream
        channelExec.setCommand(cmd)
        channelExec.connect()
        val feedback = CmdFeedbackHelper(inputStream, cmd, shell, prefix).extractFeedback()
        channelExec.disconnect()
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