package com.toocol.termio.core.file.handlers;

import com.toocol.termio.core.file.core.DirectoryChooser;
import com.toocol.termio.utilities.module.IAddress;
import com.toocol.termio.utilities.module.BlockingMessageHandler;
import com.toocol.termio.core.file.FileAddress;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/16 16:19
 */
public final class BlockingChooseDirectoryHandler extends BlockingMessageHandler<String> {

    public BlockingChooseDirectoryHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return FileAddress.CHOOSE_DIRECTORY;
    }

    @Override
    protected <T> void handleBlocking(Promise<String> promise, Message<T> message) throws Exception {
        promise.complete(new DirectoryChooser().showOpenDialog());
    }

    @Override
    protected <T> void resultBlocking(AsyncResult<String> asyncResult, Message<T> message) throws Exception {
        message.reply(asyncResult.result());
    }
}
