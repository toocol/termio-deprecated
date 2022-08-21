package com.toocol.termio.console.term.handlers;

import com.toocol.termio.core.term.TermAddress;
import com.toocol.termio.utilities.module.IAddress;
import com.toocol.termio.utilities.module.NonBlockingMessageHandler;
import com.toocol.termio.utilities.utils.StrUtil;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.jetbrains.annotations.NotNull;


/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:09
 */
public final class CleanEchoBufferHandler extends NonBlockingMessageHandler {

    public CleanEchoBufferHandler(Vertx vertx, Context context) {
        super(vertx, context);
    }

    @NotNull
    @Override
    public IAddress consume() {
        return TermAddress.TERMINAL_ECHO_CLEAN_BUFFER;
    }

    @Override
    public <T> void handleInline(@NotNull Message<T> message) {
        DynamicEchoHandler.lastInput = StrUtil.EMPTY;
    }
}
