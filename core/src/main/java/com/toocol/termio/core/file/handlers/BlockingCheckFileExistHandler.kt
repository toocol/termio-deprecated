package com.toocol.termio.core.file.handlers;

import com.toocol.termio.utilities.module.IAddress;
import com.toocol.termio.utilities.module.BlockingMessageHandler;
import com.toocol.termio.utilities.utils.FileUtil;
import com.toocol.termio.core.file.FileAddress;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 16:19
 */
public final class BlockingCheckFileExistHandler extends BlockingMessageHandler<Void> {

    public BlockingCheckFileExistHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @NotNull
    @Override
    public IAddress consume() {
        return FileAddress.CHECK_FILE_EXIST;
    }

    @Override
    protected <T> void handleBlocking(@NotNull Promise<Void> promise, @NotNull Message<T> message) throws Exception {
        String filePath = cast(message.body());
        boolean success = FileUtil.checkAndCreateFile(filePath);
        if (!success) {
            throw new RuntimeException("Create credential file failed.");
        }
        promise.complete();
    }

    @Override
    protected <T> void resultBlocking(@NotNull AsyncResult<Void> asyncResult, @NotNull Message<T> message) {
        message.reply(null);
    }
}
