package com.toocol.termio.desktop.api.term.handlers;

import com.toocol.termio.core.cache.StatusCache;
import com.toocol.termio.core.term.TermAddress;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.utilities.module.IAddress;
import com.toocol.termio.utilities.module.BlockingMessageHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.jetbrains.annotations.NotNull;

import static com.toocol.termio.core.term.TermAddress.ACCEPT_COMMAND_DESKTOP;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/15 14:25
 */
public class BlockingDesktopAcceptCommandHandler extends BlockingMessageHandler<Boolean> {

    public BlockingDesktopAcceptCommandHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @NotNull
    @Override
    public IAddress consume() {
        return ACCEPT_COMMAND_DESKTOP;
    }

    @Override
    protected <T> void handleBlocking(@NotNull Promise<Boolean> promise, @NotNull Message<T> message) {
        Term term = Term.instance;
        while (true) {
            String cmd = term.readLine();

            eventBus.send(TermAddress.EXECUTE_OUTSIDE_DESKTOP.address(), cmd);

            if (StatusCache.STOP_ACCEPT_OUT_COMMAND) {
                StatusCache.STOP_ACCEPT_OUT_COMMAND = false;
                promise.complete(false);
                break;
            }
        }
    }

    @Override
    protected <T> void resultBlocking(@NotNull AsyncResult<Boolean> asyncResult, @NotNull Message<T> message) {

    }
}
