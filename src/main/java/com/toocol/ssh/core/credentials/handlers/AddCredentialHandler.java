package com.toocol.ssh.core.credentials.handlers;

import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.utils.FileUtil;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.credentials.vo.SshCredential;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.credentials.CredentialVerticleAddress.ADD_CREDENTIAL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 16:18
 */
public class AddCredentialHandler extends AbstractMessageHandler<Boolean> {

    public AddCredentialHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress consume() {
        return ADD_CREDENTIAL;
    }

    @Override
    protected <T> void handleWithin(Promise<Boolean> promise, Message<T> message) throws Exception {
        SshCredential credential = SshCredential.transFromJson(cast(message.body()));
        CredentialCache.addCredential(credential);

        String filePath = FileUtil.relativeToFixed("/starter/credentials.json");
        vertx.fileSystem().writeFile(filePath, Buffer.buffer(CredentialCache.getCredentialsJson()), result -> {
        });

        promise.complete(true);
    }

    @Override
    protected <T> void resultWithin(AsyncResult<Boolean> asyncResult, Message<T> message) throws Exception {
        message.reply(null);
    }
}
