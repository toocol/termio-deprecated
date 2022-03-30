package com.toocol.ssh.core.command.handlers;

import com.toocol.ssh.common.handler.AbstractCommandHandler;
import com.toocol.ssh.common.router.IAddress;
import com.toocol.ssh.core.command.enums.InsideCommand;
import com.toocol.ssh.core.command.enums.OutsideCommand;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_EXECUTE_OUTSIDE;
import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_EXECUTE_SHELL;


/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:09
 */
public class ExecuteOutsideCommandHandler extends AbstractCommandHandler {

    public ExecuteOutsideCommandHandler(Vertx vertx, WorkerExecutor executor) {
        super(vertx, executor);
    }

    @Override
    public IAddress address() {
        return ADDRESS_EXECUTE_OUTSIDE;
    }

    @Override
    public <T> void handle(Message<T> message) {
        executor.executeBlocking(future -> {
            String cmd = String.valueOf(message.body());
            if (OutsideCommand.CMD_SHOW.cmd().equals(cmd)) {
                eventBus.send(ADDRESS_EXECUTE_SHELL.address(), InsideCommand.newWindowOpenssh());
            }
        }, res -> {
        });
    }
}
