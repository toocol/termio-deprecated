package com.toocol.termio.core.term.handlers.desktop;

import com.toocol.termio.core.cache.StatusCache;
import com.toocol.termio.core.term.TermAddress;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.utilities.address.IAddress;
import com.toocol.termio.utilities.handler.BlockingMessageHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.toocol.termio.core.term.TermAddress.ACCEPT_COMMAND_DESKTOP;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/15 14:25
 */
public class BlockingDesktopAcceptCommandHandler extends BlockingMessageHandler<Boolean> {

    public BlockingDesktopAcceptCommandHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return ACCEPT_COMMAND_DESKTOP;
    }

    @Override
    protected <T> void handleBlocking(Promise<Boolean> promise, Message<T> message) throws Exception {
        Term term = Term.getInstance();
        while (true) {
            term.setCursorPosition(Term.getPromptLen(), Term.executeLine);
            String cmd = term.readLine();

            CountDownLatch latch = new CountDownLatch(1);
            AtomicBoolean isBreak = new AtomicBoolean();

            eventBus.request(TermAddress.EXECUTE_OUTSIDE.address(), cmd, result -> {
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
    }

    @Override
    protected <T> void resultBlocking(AsyncResult<Boolean> asyncResult, Message<T> message) throws Exception {

    }
}
