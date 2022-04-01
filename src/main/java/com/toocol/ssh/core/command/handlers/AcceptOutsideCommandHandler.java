package com.toocol.ssh.core.command.handlers;

import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.core.command.commands.OutsideCommand;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;
import org.apache.commons.lang3.StringUtils;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_ACCEPT_COMMAND;
import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_EXECUTE_OUTSIDE;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:11
 */
public class AcceptOutsideCommandHandler extends AbstractMessageHandler<Boolean> {

    public AcceptOutsideCommandHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress consume() {
        return ADDRESS_ACCEPT_COMMAND;
    }

    @Override
    protected <T> void handleWithin(Future<Boolean> future, Message<T> message) {
        try {
            boolean needClear = cast(message.body());
            boolean tmpFlag = needClear;
            if (needClear) {
                Printer.clear();
                Printer.printScene();
            }
            while (true) {
                if (tmpFlag) {
                    Printer.printCursorLine();
                } else {
                    tmpFlag = true;
                }

                Scanner scanner = new Scanner(System.in);
                String cmd = scanner.nextLine();

                CountDownLatch latch = new CountDownLatch(1);
                AtomicBoolean isBreak = new AtomicBoolean();

                boolean isCommand = OutsideCommand.cmdOf(cmd).map(cmdCommand -> {
                    eventBus.send(ADDRESS_EXECUTE_OUTSIDE.address(), cmd, result -> {
                        String failedMsg = cast(result.result().body());
                        if (StringUtils.isNotEmpty(failedMsg)) {
                            Printer.println(failedMsg);
                        }

                        if (OutsideCommand.CMD_NUMBER.equals(cmdCommand) && StringUtils.isEmpty(failedMsg)) {
                            isBreak.set(true);
                        }

                        latch.countDown();
                    });

                    return true;
                }).orElseGet(() -> {
                    latch.countDown();
                    return  false;
                });

                latch.await();

                if (isBreak.get()) {
                    // start to accept shell's command, break the cycle.
                    future.complete(false);
                    break;
                }
                if (!isCommand) {
                    Printer.printPrompt(cmd);
                }
            }
        } catch (Exception e) {
            // to do nothing, enter the next round of accept command cycle
            future.complete(true);
        }
    }

    @Override
    protected <T> void resultWithin(AsyncResult<Boolean> asyncResult, Message<T> message) {
        if (asyncResult.result()) {
            eventBus.send(ADDRESS_ACCEPT_COMMAND.address(), false);
        }
    }
}
