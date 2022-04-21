package com.toocol.ssh.core.term.handlers;

import com.jcraft.jsch.ChannelShell;
import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractBlockingMessageHandler;
import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.term.core.Term;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.term.TermAddress.MONITOR_TERMINAL;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/13 0:58
 * @version: 0.0.1
 */
@SuppressWarnings("all")
public final class BlockingMonitorTerminalHandler extends AbstractBlockingMessageHandler<Void> {

    private final Term term = Term.getInstance();

    public BlockingMonitorTerminalHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return MONITOR_TERMINAL;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<Void> promise, Message<T> message) throws Exception {
        Long sessionId = cast(message.body());
        ChannelShell channelShell = SessionCache.getInstance().getChannelShell(sessionId);

        int currentWidth = term.getWidth();
        int currentHeight = term.getHeight();

        while (true) {
            int terminalWidth = term.getWidth();
            int terminalHeight = term.getHeight();

            if (currentWidth != terminalWidth || currentHeight != terminalHeight) {
                channelShell.setPtySize(terminalWidth, terminalHeight, terminalWidth, terminalHeight);
                currentHeight = terminalHeight;
                currentWidth = terminalWidth;
            }

            if (StatusCache.STOP_LISTEN_TERMINAL_SIZE_CHANGE) {
                StatusCache.STOP_LISTEN_TERMINAL_SIZE_CHANGE = false;
                break;
            }

            /*
             * Reduce CPU utilization
             */
            Thread.sleep(1);
        }

        promise.complete();
    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<Void> asyncResult, Message<T> message) throws Exception {

    }
}
