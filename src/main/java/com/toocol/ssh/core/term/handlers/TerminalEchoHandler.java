package com.toocol.ssh.core.term.handlers;

import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.utils.StrUtil;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.term.TermAddress.TERMINAL_ECHO;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 2:59
 * @version: 0.0.1
 */
public class TerminalEchoHandler extends AbstractMessageHandler {

    public static volatile String command = StrUtil.EMPTY;

    protected TerminalEchoHandler(Vertx vertx, Context context) {
        super(vertx, context);
    }

    @Override
    public <T> void handle(Message<T> message) {
        command = cast(message.body());
    }

    @Override
    public IAddress consume() {
        return TERMINAL_ECHO;
    }
}
