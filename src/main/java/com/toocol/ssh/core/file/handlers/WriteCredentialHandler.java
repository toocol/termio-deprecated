package com.toocol.ssh.core.file.handlers;

import com.toocol.ssh.common.handler.AbstractCommandHandler;
import com.toocol.ssh.common.router.IAddress;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.file.FileVerticleAddress.ADDRESS_WRITE_CREDENTIAL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:45
 */
public class WriteCredentialHandler extends AbstractCommandHandler {

    public WriteCredentialHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress address() {
        return ADDRESS_WRITE_CREDENTIAL;
    }

    @Override
    protected <R, T> void handleWithin(Future<R> future, Message<T> message) {

    }

    @Override
    protected <R, T> void resultWithin(AsyncResult<R> asyncResult, Message<T> message) {

    }
}
