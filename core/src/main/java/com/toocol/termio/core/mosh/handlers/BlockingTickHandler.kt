package com.toocol.termio.core.mosh.handlers;

import com.toocol.termio.core.cache.MoshSessionCache;
import com.toocol.termio.core.mosh.core.MoshSession;
import com.toocol.termio.utilities.module.BlockingMessageHandler;
import com.toocol.termio.utilities.module.IAddress;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.jetbrains.annotations.NotNull;

import static com.toocol.termio.core.mosh.MoshAddress.MOSH_TICK;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/14 21:17
 * @version: 0.0.1
 */
@SuppressWarnings("all")
public final class BlockingTickHandler extends BlockingMessageHandler<Void> {

    private final MoshSessionCache.Instance moshSessionCache = MoshSessionCache.Instance;

    public BlockingTickHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @NotNull
    @Override
    public IAddress consume() {
        return MOSH_TICK;
    }

    @Override
    protected <T> void handleBlocking(@NotNull Promise<Void> promise, @NotNull Message<T> message) throws Exception {
        long sessionId = cast(message.body());
        MoshSession moshSession = moshSessionCache.get(sessionId);

        while (moshSession != null && moshSession.isConnected()) {

            moshSession.tick();

            Thread.sleep(1);

        }

        promise.complete();
    }

    @Override
    protected <T> void resultBlocking(@NotNull AsyncResult<Void> asyncResult, @NotNull Message<T> message) throws Exception {

    }
}
