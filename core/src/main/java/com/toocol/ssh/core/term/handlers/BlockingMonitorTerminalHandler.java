package com.toocol.ssh.core.term.handlers;

import com.jcraft.jsch.ChannelShell;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.handler.AbstractBlockingMessageHandler;
import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.core.term.core.TermStatus;
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

    public static volatile long sessionId;

    public BlockingMonitorTerminalHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return MONITOR_TERMINAL;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<Void> promise, Message<T> message) throws Exception {
        int currentWidth = term.getWidth();
        int currentHeight = term.getHeight();

        while (true) {
            int terminalWidth = term.getWidth();
            int terminalHeight = term.getHeight();

            if (currentWidth != terminalWidth || currentHeight != terminalHeight) {
                if (Term.status.equals(TermStatus.SHELL)) {
                    ChannelShell channelShell = SessionCache.getInstance().getChannelShell(sessionId);
                    channelShell.setPtySize(terminalWidth, terminalHeight, terminalWidth, terminalHeight);
                } else if (Term.status.equals(TermStatus.TERMIO)) {
                    Printer.printScene(true);
                    SessionCache.getInstance()
                            .allChannelShell()
                            .forEach(channelShell -> channelShell.setPtySize(terminalWidth, terminalHeight, terminalWidth, terminalHeight));
                }
                currentHeight = terminalHeight;
                currentWidth = terminalWidth;
            }

            if (StatusCache.STOP_PROGRAM) {
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
