package com.toocol.termio.core.file.handlers

import com.toocol.termio.core.file.FileAddress
import com.toocol.termio.utilities.module.BlockingMessageHandler
import com.toocol.termio.utilities.module.IAddress
import com.toocol.termio.utilities.utils.FileUtil
import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 16:19
 */
class BlockingCheckFileExistHandler(vertx: Vertx?, context: Context?, parallel: Boolean) :
    BlockingMessageHandler<Void?>(
        vertx!!, context!!, parallel) {
    override fun consume(): IAddress {
        return FileAddress.CHECK_FILE_EXIST
    }

    @Throws(Exception::class)
    override fun <T> handleBlocking(promise: Promise<Void?>, message: Message<T>) {
        val filePath = cast<String>(message.body())
        val success = FileUtil.checkAndCreateFile(filePath)
        if (!success) {
            throw RuntimeException("Create credential file failed.")
        }
        promise.complete()
    }

    override fun <T> resultBlocking(asyncResult: AsyncResult<Void?>, message: Message<T>) {
        message.reply(null)
    }
}