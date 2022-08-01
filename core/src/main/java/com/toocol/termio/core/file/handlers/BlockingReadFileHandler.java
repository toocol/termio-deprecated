package com.toocol.termio.core.file.handlers;

import com.toocol.termio.utilities.address.IAddress;
import com.toocol.termio.utilities.handler.BlockingMessageHandler;
import com.toocol.termio.core.file.FileAddress;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:35
 */
public final class BlockingReadFileHandler extends BlockingMessageHandler<String> {

    public BlockingReadFileHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return FileAddress.READ_FILE;
    }

    @Override
    protected <T> void handleBlocking(Promise<String> promise, Message<T> message) {
        String filePath = cast(message.body());
        Buffer resultBuffer = vertx.fileSystem().readFileBlocking(filePath);
        String fileData = resultBuffer.getString(0, resultBuffer.length());

        promise.complete(fileData);
    }

    @Override
    protected <T> void resultBlocking(AsyncResult<String> asyncResult, Message<T> message) {
        String result = asyncResult.result();
        message.reply(result);
    }
}
