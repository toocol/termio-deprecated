package com.toocol.ssh.core.file.handlers;

import com.toocol.ssh.common.handler.AbstractCommandHandler;
import com.toocol.ssh.common.router.IAddress;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.file.FileVerticleAddress.ADDRESS_WRITE_CREDENTIAL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:45
 */
public class WriteCredentialHandler extends AbstractCommandHandler {

    public WriteCredentialHandler(Vertx vertx, WorkerExecutor executor) {
        super(vertx, executor);
    }

    @Override
    public IAddress address() {
        return ADDRESS_WRITE_CREDENTIAL;
    }

    @Override
    public <T> void handle(Message<T> message) {

    }
}
