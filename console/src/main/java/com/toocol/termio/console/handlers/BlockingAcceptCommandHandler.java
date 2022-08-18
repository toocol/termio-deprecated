package com.toocol.termio.console.handlers;

import com.toocol.termio.core.cache.StatusCache;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.utilities.module.IAddress;
import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.module.BlockingMessageHandler;
import com.toocol.termio.utilities.utils.MessageBox;
import com.toocol.termio.core.term.TermAddress;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.toocol.termio.core.cache.StatusConstants.*;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:11
 */
public final class BlockingAcceptCommandHandler extends BlockingMessageHandler<Boolean> {

    private final Term term = Term.getInstance();

    public BlockingAcceptCommandHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return TermAddress.ACCEPT_COMMAND_CONSOLE;
    }

    @Override
    protected <T> void handleBlocking(Promise<Boolean> promise, Message<T> message) {
        try {
            int signal = cast(message.body());
            if (signal == NORMAL_BACK || signal == FIRST_IN || signal == CONNECT_FAILED) {
                Printer.clear();
                Term.getInstance().printScene(false);
            }

            term.printExecuteBackground();
            if (signal == CONNECT_FAILED) {
                term.printErr("lost connection.");
            }
            if (MessageBox.hasMessage()) {
                term.printDisplay(MessageBox.message());
                MessageBox.clearMessage();
            }
            if (MessageBox.hasErrorMessage()) {
                term.printErr(MessageBox.errorMessage());
                MessageBox.clearErrorMessage();
            }

            term.showCursor();
            while (true) {
                term.setCursorPosition(Term.getPromptLen(), Term.executeLine);
                String cmd = term.readLine();

                CountDownLatch latch = new CountDownLatch(1);
                AtomicBoolean isBreak = new AtomicBoolean();

                eventBus.request(TermAddress.EXECUTE_OUTSIDE_CONSOLE.address(), cmd, result -> {
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
            eventBus.send(TermAddress.ACCEPT_COMMAND_CONSOLE.address(), ACCEPT_ERROR);
        }
    }
}
