package com.toocol.termio.core.mosh.handlers;

import com.toocol.termio.core.cache.MoshSessionCache;
import com.toocol.termio.utilities.address.IAddress;
import com.toocol.termio.utilities.handler.NonBlockingMessageHandler;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import java.util.Optional;

import static com.toocol.termio.core.mosh.MoshAddress.LISTEN_LOCAL_SOCKET;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/30 19:24
 * @version: 0.0.1
 */
public final class SocketListenHandler extends NonBlockingMessageHandler {

    private final MoshSessionCache moshSessionCache = MoshSessionCache.getInstance();

    public SocketListenHandler(Vertx vertx, Context context) {
        super(vertx, context);
    }

    @Override
    public <T> void handleInline(Message<T> message) {
        Optional.ofNullable(moshSessionCache.get(cast(message.body())))
                .ifPresent(moshSession -> moshSession.connect(message));
    }

    @Override
    public IAddress consume() {
        return LISTEN_LOCAL_SOCKET;
    }
}
