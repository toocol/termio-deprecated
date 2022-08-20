package com.toocol.termio.core.file.handlers;

import com.toocol.termio.core.file.core.FileChooser;
import com.toocol.termio.utilities.module.IAddress;
import com.toocol.termio.utilities.module.BlockingMessageHandler;
import com.toocol.termio.core.file.FileAddress;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.jetbrains.annotations.NotNull;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/10 19:34
 * @version: 0.0.1
 */
public final class BlockingChooseFileHandler extends BlockingMessageHandler<String> {

    public BlockingChooseFileHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @NotNull
    @Override
    public IAddress consume() {
        return FileAddress.CHOOSE_FILE;
    }

    @Override
    protected <T> void handleBlocking(@NotNull Promise<String> promise, @NotNull Message<T> message) throws Exception {
        FileChooser fileChooser = new FileChooser();
        promise.complete(fileChooser.showOpenDialog());
    }

    @Override
    protected <T> void resultBlocking(@NotNull AsyncResult<String> asyncResult, @NotNull Message<T> message) throws Exception {
        message.reply(asyncResult.result());
    }
}
