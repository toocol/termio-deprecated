package com.toocol.ssh.core.file.handlers;

import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.core.file.core.DirectoryChooser;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.file.FileAddress.CHOOSE_DIRECTORY;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/16 16:19
 */
public final class ChooseDirectoryHandler extends AbstractMessageHandler<String> {

    public ChooseDirectoryHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return CHOOSE_DIRECTORY;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<String> promise, Message<T> message) throws Exception {
        promise.complete(new DirectoryChooser().showOpenDialog());
    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<String> asyncResult, Message<T> message) throws Exception {
        message.reply(asyncResult.result());
    }
}
