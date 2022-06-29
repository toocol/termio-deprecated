package com.toocol.ssh.utilities.handler;

import com.toocol.ssh.utilities.utils.ExitMessage;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/27 10:40
 */
public abstract class NonBlockingMessageHandler extends AbstractMessageHandler {

    protected NonBlockingMessageHandler(Vertx vertx, Context context) {
        super(vertx, context);
    }

    @Override
    public <T> void handle(Message<T> message) {
        try {
            handleInline(message);
        } catch (Exception e) {
            ExitMessage.setMsg("Caught exception, exit program, message = " + e.getMessage());
            error("Caught exception, exit program, message = {}", e.getMessage());
            System.exit(-1);
        }
    }

    public abstract <T> void handleInline(Message<T> message);
}
