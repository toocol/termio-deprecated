package com.toocol.ssh.core.auth.handlers;

import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.utils.FileUtil;
import com.toocol.ssh.core.cache.CredentialCache;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.auth.AuthAddress.DELETE_CREDENTIAL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 16:18
 */
public class DeleteCredentialHandler extends AbstractMessageHandler<Boolean> {

    public DeleteCredentialHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress consume() {
        return DELETE_CREDENTIAL;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<Boolean> promise, Message<T> message) throws Exception {
        int index = cast(message.body());
        CredentialCache.deleteCredential(index);

        String filePath = FileUtil.relativeToFixed("./credentials.json");
        vertx.fileSystem().writeFile(filePath, Buffer.buffer(CredentialCache.getCredentialsJson()), result -> {
        });

        promise.complete(true);
    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<Boolean> asyncResult, Message<T> message) throws Exception {
        message.reply(null);
    }
}
