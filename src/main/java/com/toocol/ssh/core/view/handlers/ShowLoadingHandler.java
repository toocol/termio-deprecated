package com.toocol.ssh.core.view.handlers;

import com.toocol.ssh.common.handler.AbstractCommandHandler;
import com.toocol.ssh.common.router.IAddress;
import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_CLEAR;
import static com.toocol.ssh.core.view.TerminalViewAddress.ADDRESS_LOADING;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:50
 */
public class ShowLoadingHandler extends AbstractCommandHandler {

    public ShowLoadingHandler(Vertx vertx, WorkerExecutor executor) {
        super(vertx, executor);
    }

    @Override
    public IAddress address() {
        return ADDRESS_LOADING;
    }

    @Override
    public <T> void handle(Message<T> message) {
        executor.executeBlocking(future -> {
            PrintUtil.loading();
            future.complete("loaded");
        }, res -> eventBus.send(ADDRESS_CLEAR.address(), "start"));
    }
}
