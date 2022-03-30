package com.toocol.ssh.core.command.handlers;

import com.toocol.ssh.common.handler.AbstractCommandHandler;
import com.toocol.ssh.common.router.IAddress;
import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_CLEAR;
import static com.toocol.ssh.core.configuration.vert.ConfigurationVerticle.*;
import static com.toocol.ssh.core.view.TerminalViewAddress.ADDRESS_SCREEN_HAS_CLEARED;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:04
 */
public class ClearScreenHandler extends AbstractCommandHandler {

    public ClearScreenHandler(Vertx vertx, WorkerExecutor executor) {
        super(vertx, executor);
    }

    @Override
    public IAddress address() {
        return ADDRESS_CLEAR;
    }

    @Override
    public <T> void handle(Message<T> message) {
        executor.executeBlocking(future -> {
            try {
                new ProcessBuilder(BOOT_TYPE, getExtraCmd(), getClearCmd())
                        .inheritIO()
                        .start()
                        .waitFor();
                future.complete("cleared");
            } catch (Exception e) {
                PrintUtil.printErr("execute command failed!!");
                future.complete("failed");
            }
        }, res -> eventBus.send(ADDRESS_SCREEN_HAS_CLEARED.address(), "cleared"));
    }
}
