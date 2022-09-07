package com.toocol.termio.core.mosh.handlers;

import com.toocol.termio.core.cache.MoshSessionCache;
import com.toocol.termio.utilities.module.IAddress;
import com.toocol.termio.utilities.module.NonBlockingMessageHandler;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Optional;

import static com.toocol.termio.core.mosh.MoshAddress.LISTEN_LOCAL_SOCKET;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/30 19:24
 * @version: 0.0.1
 */
public final class SocketListenHandler extends NonBlockingMessageHandler {

    private final MoshSessionCache.Instance moshSessionCache = MoshSessionCache.Instance;

    public SocketListenHandler(Vertx vertx, Context context) {
        super(vertx, context);
    }

    @Override
    public <T> void handleInline(@NotNull Message<T> message) {
        Optional.ofNullable(moshSessionCache.get(cast(message.body())))
                .ifPresent(moshSession -> moshSession.connect(message));
    }

    @NotNull
    @Override
    public IAddress consume() {
        return LISTEN_LOCAL_SOCKET;
    }
}
