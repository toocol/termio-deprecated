package com.toocol.termio.core.file.handlers

import com.toocol.termio.core.file.FileAddress
import com.toocol.termio.utilities.module.BlockingMessageHandler
import com.toocol.termio.utilities.module.IAddress
import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:35
 */
class BlockingReadFileHandler(vertx: Vertx?, context: Context?, parallel: Boolean) : BlockingMessageHandler<String?>(
    vertx!!, context!!, parallel) {
    override fun consume(): IAddress {
        return FileAddress.READ_FILE
    }

    override fun <T> handleBlocking(promise: Promise<String?>, message: Message<T>) {
        val filePath = cast<String>(message.body())
        val resultBuffer = vertx.fileSystem().readFileBlocking(filePath)
        val fileData = resultBuffer.getString(0, resultBuffer.length())
        promise.complete(fileData)
    }

    override fun <T> resultBlocking(asyncResult: AsyncResult<String?>, message: Message<T>) {
        val result = asyncResult.result()
        message.reply(result)
    }
}