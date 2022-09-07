package com.toocol.termio.core.file.handlers

import com.toocol.termio.core.file.FileAddress
import com.toocol.termio.core.file.core.FileChooser
import com.toocol.termio.utilities.module.BlockingMessageHandler
import com.toocol.termio.utilities.module.IAddress
import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/10 19:34
 * @version: 0.0.1
 */
class BlockingChooseFileHandler(vertx: Vertx?, context: Context?, parallel: Boolean) : BlockingMessageHandler<String?>(
    vertx!!, context!!, parallel) {
    override fun consume(): IAddress {
        return FileAddress.CHOOSE_FILE
    }

    @Throws(Exception::class)
    override fun <T> handleBlocking(promise: Promise<String?>, message: Message<T>) {
        val fileChooser = FileChooser()
        promise.complete(fileChooser.showOpenDialog())
    }

    @Throws(Exception::class)
    override fun <T> resultBlocking(asyncResult: AsyncResult<String?>, message: Message<T>) {
        message.reply(asyncResult.result())
    }
}