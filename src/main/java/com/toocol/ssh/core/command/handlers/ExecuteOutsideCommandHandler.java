package com.toocol.ssh.core.command.handlers;

import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.router.IAddress;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.command.commands.OutsideCommand;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_EXECUTE_OUTSIDE;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:09
 */
public class ExecuteOutsideCommandHandler extends AbstractMessageHandler<Void> {

    public ExecuteOutsideCommandHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress address() {
        return ADDRESS_EXECUTE_OUTSIDE;
    }

    @Override
    protected <T> void handleWithin(Future<Void> future, Message<T> message) {
        String cmd = String.valueOf(message.body());
        OutsideCommand.cmdOf(cmd)
                .ifPresent(outsideCommand -> {
                    try {
                        outsideCommand.processCmd(eventBus, null);
                    } catch (Exception e) {
                        PrintUtil.printErr("Execute command failed, message = " + e.getMessage());
                    }
                });
        future.complete();
    }

    @Override
    protected <T> void resultWithin(AsyncResult<Void> asyncResult, Message<T> message) {
        message.reply(null);
    }
}
