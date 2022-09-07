package com.toocol.termio.core.shell.handlers

import com.toocol.termio.core.cache.ShellCache.Instance.getShell
import com.toocol.termio.core.file.FileAddress
import com.toocol.termio.core.shell.ShellAddress
import com.toocol.termio.core.shell.core.SftpChannelProvider
import com.toocol.termio.utilities.ansi.Printer.print
import com.toocol.termio.utilities.ansi.Printer.println
import com.toocol.termio.utilities.module.BlockingMessageHandler
import com.toocol.termio.utilities.module.IAddress
import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import org.apache.commons.io.IOUtils
import java.util.*

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/9 16:39
 * @version: 0.0.1
 */
class BlockingDfHandler(vertx: Vertx?, context: Context?, parallel: Boolean) : BlockingMessageHandler<ByteArray?>(
    vertx!!, context!!, parallel) {

    private val sftpChannelProvider = SftpChannelProvider.Instance

    override fun consume(): IAddress {
        return ShellAddress.START_DF_COMMAND
    }

    @Throws(Exception::class)
    override fun <T> handleBlocking(promise: Promise<ByteArray?>, message: Message<T>) {
        val request = cast<JsonObject>(message.body())
        val sessionId = request.getLong("sessionId")
        val remotePath = request.getString("remotePath")
        val type = Optional.ofNullable(request.getInteger("type")).orElse(0)
        if (type != DF_TYPE_FILE && type != DF_TYPE_BYTE) {
            promise.complete()
            return
        }
        val channelSftp = sftpChannelProvider.getChannelSftp(sessionId)
        if (channelSftp == null) {
            val shell = getShell(sessionId) ?: return
            shell.printErr("Create sftp channel failed.")
            promise.complete()
            return
        }
        if (type == DF_TYPE_FILE) {
            eventBus.request(FileAddress.CHOOSE_DIRECTORY.address(), null) { result: AsyncResult<Message<Any?>?> ->
                val storagePath: String = if (result.result() == null) {
                    "-1"
                } else {
                    cast(Objects.requireNonNullElse(result.result()!!.body(), "-1"))
                }
                val shell = getShell(sessionId)
                if (shell == null) {
                    promise.fail("-1")
                    promise.tryComplete()
                    return@request
                }
                print(shell.getPrompt())
                if ("-1" == storagePath) {
                    promise.fail("-1")
                    promise.tryComplete()
                    return@request
                }
                if (remotePath.contains(",")) {
                    for (rpath in remotePath.split(",").toTypedArray()) {
                        try {
                            channelSftp[rpath, storagePath]
                        } catch (e: Exception) {
                            println("\ndf: no such file '$rpath'.")
                            print(shell.getPrompt() + shell.getCurrentPrint())
                        }
                    }
                } else {
                    try {
                        channelSftp[remotePath, storagePath]
                    } catch (e: Exception) {
                        println("\ndf: no such file '$remotePath'.")
                        print(shell.getPrompt() + shell.getCurrentPrint())
                    }
                }
            }
            promise.tryComplete()
        } else {
            try {
                val inputStream = channelSftp[remotePath]
                val bytes = IOUtils.buffer(inputStream).readAllBytes()
                promise.tryComplete(bytes)
            } catch (e: Exception) {
                promise.tryComplete()
            }
        }
    }

    @Throws(Exception::class)
    override fun <T> resultBlocking(asyncResult: AsyncResult<ByteArray?>, message: Message<T>) {
        val result = asyncResult.result()
        if (result != null && result.isNotEmpty()) {
            message.reply(result)
        }
    }

    companion object {
        const val DF_TYPE_FILE = 1
        const val DF_TYPE_BYTE = 2
    }
}