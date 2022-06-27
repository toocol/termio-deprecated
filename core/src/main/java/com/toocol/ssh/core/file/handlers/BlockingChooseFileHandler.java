package com.toocol.ssh.core.file.handlers;

import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.handler.BlockingMessageHandler;
import com.toocol.ssh.core.file.core.FileChooser;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.file.FileAddress.CHOOSE_FILE;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/10 19:34
 * @version: 0.0.1
 */
public final class BlockingChooseFileHandler extends BlockingMessageHandler<String> {

    public BlockingChooseFileHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return CHOOSE_FILE;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<String> promise, Message<T> message) throws Exception {
        FileChooser fileChooser = new FileChooser();
        promise.complete(fileChooser.showOpenDialog());
    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<String> asyncResult, Message<T> message) throws Exception {
        message.reply(asyncResult.result());
    }
}
