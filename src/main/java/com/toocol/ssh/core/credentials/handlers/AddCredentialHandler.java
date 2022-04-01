package com.toocol.ssh.core.credentials.handlers;

import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.core.cache.Cache;
import com.toocol.ssh.core.credentials.vo.SshCredential;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

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
    protected <T> void handleWithin(Future<Boolean> future, Message<T> message) throws Exception {
        SshCredential credential = SshCredential.transFromJson(cast(message.body()));
        Cache.addCredential(credential);
        future.complete(true);
    }

    @Override
    protected <T> void resultWithin(AsyncResult<Boolean> asyncResult, Message<T> message) throws Exception {

    }
}
