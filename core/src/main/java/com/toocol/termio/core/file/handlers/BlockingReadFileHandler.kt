package com.toocol.termio.core.file.handlers;

import com.toocol.termio.utilities.module.IAddress;
import com.toocol.termio.utilities.module.BlockingMessageHandler;
import com.toocol.termio.core.file.FileAddress;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:35
 */
public final class BlockingReadFileHandler extends BlockingMessageHandler<String> {

    public BlockingReadFileHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @NotNull
    @Override
    public IAddress consume() {
        return FileAddress.READ_FILE;
    }

    @Override
    protected <T> void handleBlocking(@NotNull Promise<String> promise, @NotNull Message<T> message) {
        String filePath = cast(message.body());
        Buffer resultBuffer = vertx.fileSystem().readFileBlocking(filePath);
        String fileData = resultBuffer.getString(0, resultBuffer.length());

        promise.complete(fileData);
    }

    @Override
    protected <T> void resultBlocking(@NotNull AsyncResult<String> asyncResult, @NotNull Message<T> message) {
        String result = asyncResult.result();
        message.reply(result);
    }
}
