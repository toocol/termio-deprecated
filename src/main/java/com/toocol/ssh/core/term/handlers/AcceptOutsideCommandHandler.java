package com.toocol.ssh.core.term.handlers;

import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.term.commands.OutsideCommand;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.core.Termio;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.toocol.ssh.core.term.TermAddress.ADDRESS_ACCEPT_COMMAND;
import static com.toocol.ssh.core.term.TermAddress.ADDRESS_EXECUTE_OUTSIDE;


/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:11
 */
public class AcceptOutsideCommandHandler extends AbstractMessageHandler<Boolean> {

    private static final int FIRST_IN = 0;
    private static final int SELF_ERROR = 1;
    private static final int ERROR_BACK = 2;
    private static final int NORMAL_BACK = 3;

    public AcceptOutsideCommandHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress consume() {
        return ADDRESS_ACCEPT_COMMAND;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<Boolean> promise, Message<T> message) {
        try {
            int signal = cast(message.body());
            if (signal == NORMAL_BACK || signal == FIRST_IN) {
                Printer.clear();
                Printer.printScene();
            }
            while (true) {
                if (signal == SELF_ERROR) {
                    signal = FIRST_IN;
                } else {
                    Printer.printCursorLine();
                }

                String cmd = Termio.getInstance().getReader().readLine();

                CountDownLatch latch = new CountDownLatch(1);
                AtomicBoolean isBreak = new AtomicBoolean();

                boolean isCommand = OutsideCommand.cmdOf(cmd).map(cmdCommand -> {
                    eventBus.request(ADDRESS_EXECUTE_OUTSIDE.address(), cmd, result -> {
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
                    return false;
                });

                latch.await();

                if (isBreak.get()) {
                    // start to accept shell's command, break the cycle.
                    promise.complete(false);
                    break;
                }
                if (StatusCache.STOP_ACCEPT_OUT_COMMAND) {
                    StatusCache.STOP_ACCEPT_OUT_COMMAND = false;
                    promise.complete(false);
                    break;
                }
                if (!isCommand && StringUtils.isNotEmpty(cmd)) {
                    Printer.printPrompt(cmd);
                }
            }
        } catch (Exception e) {
            // to do nothing, enter the next round of accept command cycle
            promise.complete(true);
        }
    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<Boolean> asyncResult, Message<T> message) {
        if (asyncResult.result()) {
            eventBus.send(ADDRESS_ACCEPT_COMMAND.address(), 1);
        }
    }
}
