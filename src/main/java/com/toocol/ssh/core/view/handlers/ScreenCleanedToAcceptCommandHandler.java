package com.toocol.ssh.core.view.handlers;

import com.toocol.ssh.common.handler.AbstractCommandHandler;
import com.toocol.ssh.common.router.IAddress;
import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_ACCEPT_COMMAND;
import static com.toocol.ssh.core.view.TerminalViewAddress.ADDRESS_SCREEN_HAS_CLEARED;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:51
 */
public class ScreenCleanedToAcceptCommandHandler extends AbstractCommandHandler {

    public ScreenCleanedToAcceptCommandHandler(Vertx vertx, WorkerExecutor executor) {
        super(vertx, executor);
    }

    @Override
    public IAddress address() {
        return ADDRESS_SCREEN_HAS_CLEARED;
    }

    @Override
    public <T> void handle(Message<T> message) {
        PrintUtil.printPromptScene();
        eventBus.send(ADDRESS_ACCEPT_COMMAND.address(), "start");
    }
}
