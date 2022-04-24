package com.toocol.ssh.core.term.handlers;

import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.handler.AbstractMessageHandler;
import com.toocol.ssh.core.term.core.Term;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.term.TermAddress.TERMINAL_ECHO;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 2:59
 * @version: 0.0.1
 */
public final class DynamicEchoHandler extends AbstractMessageHandler {

    public DynamicEchoHandler(Vertx vertx, Context context) {
        super(vertx, context);
    }

    @Override
    public <T> void handle(Message<T> message) {
        Term term = Term.getInstance();
        String command = cast(message.body());

        // TODO: extract the dynamic prompt
    }

    @Override
    public IAddress consume() {
        return TERMINAL_ECHO;
    }
}
