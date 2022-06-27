package com.toocol.ssh.core.term.handlers;

import com.toocol.ssh.core.cache.ShellCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.core.term.core.TermStatus;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.console.Console;
import com.toocol.ssh.utilities.handler.BlockingMessageHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.term.TermAddress.MONITOR_TERMINAL;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/13 0:58
 * @version: 0.0.1
 */
@SuppressWarnings("all")
public final class BlockingMonitorTerminalHandler extends BlockingMessageHandler<Void> {

    private final Console console = Console.get();

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
        Term.WIDTH = console.getWindowWidth();
        Term.HEIGHT = console.getWindowHeight();

        while (true) {
            int terminalWidth = console.getWindowWidth();
            int terminalHeight = console.getWindowHeight();

            if (Term.WIDTH != terminalWidth || Term.HEIGHT != terminalHeight) {
                Term.WIDTH = terminalWidth;
                Term.HEIGHT = terminalHeight;
                if (Term.status.equals(TermStatus.SHELL)) {
                    ShellCache.getInstance().getShell(sessionId).resize(terminalWidth, terminalHeight, sessionId);
                } else if (Term.status.equals(TermStatus.TERMIO)) {
                    Printer.printScene(true);
                }
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
