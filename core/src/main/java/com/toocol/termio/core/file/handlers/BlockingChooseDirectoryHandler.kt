package com.toocol.termio.core.file.handlers

import com.toocol.termio.core.file.FileAddress
import com.toocol.termio.core.file.core.DirectoryChooser
import com.toocol.termio.utilities.module.BlockingMessageHandler
import com.toocol.termio.utilities.module.IAddress
import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/16 16:19
 */
class BlockingChooseDirectoryHandler(vertx: Vertx?, context: Context?, parallel: Boolean) :
    BlockingMessageHandler<String?>(
        vertx!!, context!!, parallel) {
    override fun consume(): IAddress {
        return FileAddress.CHOOSE_DIRECTORY
    }

    @Throws(Exception::class)
    override fun <T> handleBlocking(promise: Promise<String?>, message: Message<T>) {
        promise.complete(DirectoryChooser().showOpenDialog())
    }

    @Throws(Exception::class)
    override fun <T> resultBlocking(asyncResult: AsyncResult<String?>, message: Message<T>) {
        message.reply(asyncResult.result())
    }
}