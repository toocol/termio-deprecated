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
        try {
            boolean needClear = cast(message.body());
            boolean tmpFlag = needClear;
            if (needClear) {
                PrintUtil.clear();
                PrintUtil.printScene(null);
            }
            while (true) {
                if (tmpFlag) {
                    PrintUtil.printCursorLine();
                } else {
                    tmpFlag = true;
                }

                Scanner scanner = new Scanner(System.in);
                String cmd = scanner.nextLine();
                if (OutsideCommand.isOutsideCommand(cmd)) {
                    if (OutsideCommand.CMD_CONC.cmd().equals(cmd)) {
                        eventBus.send(ADDRESS_EXECUTE_OUTSIDE.address(), cmd);
                        break;
                    }
                    CountDownLatch latch = new CountDownLatch(1);
                    eventBus.send(ADDRESS_EXECUTE_OUTSIDE.address(), cmd, result -> latch.countDown());
                    latch.await();
                } else {
                    PrintUtil.printPrompt(cmd);
                }
            }
        } catch (Exception e) {
            // to do nothing, enter the next round of accept command cycle
            future.complete();
        }
    }

    @Override
    protected <T> void resultWithin(AsyncResult<Void> asyncResult, Message<T> message) {
        eventBus.send(ADDRESS_ACCEPT_COMMAND.address(), false);
    }
}
