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

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_ACCEPT_COMMAND;
import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_EXECUTE_OUTSIDE;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:11
 */
public class AcceptOutsideCommandHandler extends AbstractMessageHandler<Void> {

    public AcceptOutsideCommandHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress address() {
        return ADDRESS_ACCEPT_COMMAND;
    }

    @Override
    protected <T> void handleWithin(Future<Void> future, Message<T> message) {
        while (true) {
            try {
                PrintUtil.printCursorLine();
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine();
                if (OutsideCommand.isOutsideCommand(input)) {
                    if (OutsideCommand.CMD_CONC.cmd().equals(input)) {
                        eventBus.send(ADDRESS_EXECUTE_OUTSIDE.address(), input);
                        break;
                    }
                    CountDownLatch latch = new CountDownLatch(1);
                    eventBus.send(ADDRESS_EXECUTE_OUTSIDE.address(), input, result -> latch.countDown());
                    latch.await();
                }
            } catch (Exception e) {
                PrintUtil.printErr("Application run failed, now exit.");
                System.exit(-1);
            }
        }
    }

    @Override
    protected <T> void resultWithin(AsyncResult<Void> asyncResult, Message<T> message) {

    }
}
