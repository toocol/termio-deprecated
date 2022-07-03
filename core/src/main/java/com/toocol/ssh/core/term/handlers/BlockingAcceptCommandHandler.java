package com.toocol.ssh.core.term.handlers;

import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.handler.BlockingMessageHandler;
import com.toocol.ssh.utilities.status.StatusCache;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.toocol.ssh.core.term.TermAddress.ACCEPT_COMMAND;
import static com.toocol.ssh.core.term.TermAddress.EXECUTE_OUTSIDE;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:11
 */
public final class BlockingAcceptCommandHandler extends BlockingMessageHandler<Boolean> {

    private final Term term = Term.getInstance();

    public static final int FIRST_IN = 0;
    public static final int NORMAL_BACK = 1;
    public static final int ACCEPT_ERROR = 2;
    public static final int CONNECT_FAILED = 3;

    public BlockingAcceptCommandHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return ACCEPT_COMMAND;
    }

    @Override
    protected <T> void handleBlocking(Promise<Boolean> promise, Message<T> message) {
        try {
            int signal = cast(message.body());
            if (signal == NORMAL_BACK || signal == FIRST_IN || signal == CONNECT_FAILED) {
                Printer.clear();
                Printer.printScene(false);
            }

            term.printExecuteBackground();
            if (signal == CONNECT_FAILED) {
                term.printErr("lost connection.");
            }
            term.showCursor();
            while (true) {
                term.setCursorPosition(Term.getPromptLen(), Term.executeLine);
                String cmd = term.readLine();

                CountDownLatch latch = new CountDownLatch(1);
                AtomicBoolean isBreak = new AtomicBoolean();

                eventBus.request(EXECUTE_OUTSIDE.address(), cmd, result -> {
                    isBreak.set(cast(result.result().body()));
                    latch.countDown();
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
            }
        } catch (Exception e) {
            // to do nothing, enter the next round of accept command cycle
            promise.complete(true);
        }
    }

    @Override
    protected <T> void resultBlocking(AsyncResult<Boolean> asyncResult, Message<T> message) {
        if (asyncResult.result()) {
            eventBus.send(ACCEPT_COMMAND.address(), ACCEPT_ERROR);
        }
    }
}
