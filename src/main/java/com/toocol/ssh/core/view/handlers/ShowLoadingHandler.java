package com.toocol.ssh.core.view.handlers;

import com.toocol.ssh.common.handler.AbstractCommandHandler;
import com.toocol.ssh.common.router.IAddress;
import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_ACCEPT_COMMAND;
import static com.toocol.ssh.core.view.TerminalViewAddress.ADDRESS_LOADING;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:50
 */
public class ShowLoadingHandler extends AbstractCommandHandler<Void> {

    public ShowLoadingHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress address() {
        return ADDRESS_LOADING;
    }

    @Override
    protected <T> void handleWithin(Future<Void> future, Message<T> message) throws Exception {
        PrintUtil.loading();
        PrintUtil.clear();
        future.complete(null);
    }

    @Override
    protected <T> void resultWithin(AsyncResult<Void> asyncResult, Message<T> message) {
        eventBus.send(ADDRESS_ACCEPT_COMMAND.address(), "start");
    }
}
