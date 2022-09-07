package com.toocol.termio.core.shell.handlers

import com.toocol.termio.core.cache.ShellCache
import com.toocol.termio.core.file.FileAddress
import com.toocol.termio.core.shell.ShellAddress
import com.toocol.termio.core.shell.core.SftpChannelProvider
import com.toocol.termio.utilities.ansi.Printer.print
import com.toocol.termio.utilities.module.BlockingMessageHandler
import com.toocol.termio.utilities.module.IAddress
import com.toocol.termio.utilities.utils.FileNameUtil
import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import java.io.FileInputStream
import java.util.*

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/9 16:38
 * @version: 0.0.1
 */
class BlockingUfHandler(vertx: Vertx?, context: Context?, parallel: Boolean) : BlockingMessageHandler<Void?>(
    vertx!!, context!!, parallel) {

    private val sftpChannelProvider = SftpChannelProvider.Instance
    private val shellCache = ShellCache.Instance

    override fun consume(): IAddress {
        return ShellAddress.START_UF_COMMAND
    }

    @Throws(Exception::class)
    override fun <T> handleBlocking(promise: Promise<Void?>, message: Message<T>) {
        val request = cast<JsonObject>(message.body())
        val sessionId = request.getLong("sessionId")
        val remotePath = request.getString("remotePath")
        val channelSftp = sftpChannelProvider.getChannelSftp(sessionId)
        if (channelSftp == null) {
            val shell = shellCache.getShell(sessionId) ?: return
            shell.printErr("Create sftp channel failed.")
            promise.complete()
            return
        }
        val localPathBuilder = StringBuilder()
        eventBus.request(FileAddress.CHOOSE_FILE.address(), null) { result: AsyncResult<Message<Any?>> ->
            localPathBuilder.append(Objects.requireNonNullElse(result.result().body(), "-1"))
            val shell = shellCache.getShell(sessionId)
            if (shell == null) {
                promise.tryFail("-1")
                return@request
            }
            print(shell.getPrompt())
            val fileNames = localPathBuilder.toString()
            if ("-1" == fileNames) {
                promise.tryFail("-1")
                return@request
            }
            try {
                channelSftp.cd(remotePath)
                for (fileName in fileNames.split(",").toTypedArray()) {
                    channelSftp.put(FileInputStream(fileName), FileNameUtil.getName(fileName))
                }
            } catch (e: Exception) {
                // do nothing
            }
        }
        promise.tryComplete()
    }

    @Throws(Exception::class)
    override fun <T> resultBlocking(asyncResult: AsyncResult<Void?>, message: Message<T>) {
    }
}