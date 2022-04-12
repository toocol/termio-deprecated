package com.toocol.ssh.core.term.handlers;

import com.jcraft.jsch.ChannelShell;
import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.cache.StatusCache;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.term.TermAddress.LISTEN_TERMINAL_SIZE_CHANGE;
import static com.toocol.ssh.core.term.vert.TermVerticle.TERMINAL;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/13 0:58
 * @version: 0.0.1
 */
public class ListenTerminalSizeHandler extends AbstractMessageHandler<Void> {

    public ListenTerminalSizeHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress consume() {
        return LISTEN_TERMINAL_SIZE_CHANGE;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<Void> promise, Message<T> message) throws Exception {
        Long sessionId = cast(message.body());
        ChannelShell channelShell = SessionCache.getInstance().getChannelShell(sessionId);

        int currentWidth = TERMINAL.getTerminalWidth();
        int currentHeight = TERMINAL.getTerminalHeight();

        while (true) {
            int terminalWidth = TERMINAL.getTerminalWidth();
            int terminalHeight = TERMINAL.getTerminalHeight();

            if (currentWidth != terminalWidth || currentHeight != terminalHeight) {
                channelShell.setPtySize(terminalWidth, terminalHeight, terminalWidth, terminalHeight);
                currentHeight = terminalHeight;
                currentWidth = terminalWidth;
            }

            if (StatusCache.STOP_LISTEN_TERMINAL_SIZE_CHANGE) {
                StatusCache.STOP_LISTEN_TERMINAL_SIZE_CHANGE = false;
                break;
            }
        }

        promise.complete();
    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<Void> asyncResult, Message<T> message) throws Exception {

    }
}
