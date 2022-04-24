package com.toocol.ssh.core.file.handlers;

import com.toocol.ssh.utilities.handler.AbstractBlockingMessageHandler;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.utils.FileUtil;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.file.FileAddress.CHECK_FILE_EXIST;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 16:19
 */
public final class BlockingCheckFileExistHandler extends AbstractBlockingMessageHandler<Void> {

    public BlockingCheckFileExistHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return CHECK_FILE_EXIST;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<Void> promise, Message<T> message) throws Exception{
        String filePath = cast(message.body());
        boolean success = FileUtil.checkAndCreateFile(filePath);
        if (!success) {
            throw new RuntimeException("Create credential file failed.");
        }
        promise.complete();
    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<Void> asyncResult, Message<T> message) {
        message.reply(null);
    }
}
