package com.toocol.ssh.core.file.handlers;

import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.utils.FileUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.file.FileVerticleAddress.ADDRESS_CHECK_FILE_EXIST;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 16:19
 */
public class CheckFileExistHandler extends AbstractMessageHandler<Void> {

    public CheckFileExistHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress consume() {
        return ADDRESS_CHECK_FILE_EXIST;
    }

    @Override
    protected <T> void handleWithin(Future<Void> future, Message<T> message) throws Exception{
        String filePath = cast(message.body());
        boolean success = FileUtils.checkAndCreateFile(filePath);
        if (!success) {
            throw new RuntimeException("Create credential file failed.");
        }
        future.complete();
    }

    @Override
    protected <T> void resultWithin(AsyncResult<Void> asyncResult, Message<T> message) {
        message.reply(null);
    }
}
