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
public class ShowLoadingHandler extends AbstractCommandHandler {

    public ShowLoadingHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress address() {
        return ADDRESS_LOADING;
    }

    @Override
    protected <R, T> void handleWithin(Future<R> future, Message<T> message) {
        try {
            PrintUtil.loading();
            PrintUtil.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        future.complete(cast("loaded"));
    }

    @Override
    protected <R, T> void resultWithin(AsyncResult<R> asyncResult, Message<T> message) {
        eventBus.send(ADDRESS_ACCEPT_COMMAND.address(), "start");
    }
}
