package com.toocol.termio.console.handlers;

import com.toocol.termio.core.cache.CredentialCache;
import com.toocol.termio.core.cache.ShellCache;
import com.toocol.termio.core.cache.SshSessionCache;
import com.toocol.termio.core.cache.StatusCache;
import com.toocol.termio.core.ssh.core.SshSessionFactory;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.core.term.core.TermStatus;
import com.toocol.termio.utilities.address.IAddress;
import com.toocol.termio.utilities.console.Console;
import com.toocol.termio.utilities.handler.BlockingMessageHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import static com.toocol.termio.core.term.TermAddress.MONITOR_TERMINAL;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/13 0:58
 * @version: 0.0.1
 */
@SuppressWarnings("all")
public final class BlockingMonitorTerminalHandler extends BlockingMessageHandler<Void> {

    private final Console console = Console.get();
    private final CredentialCache credentialCache = CredentialCache.getInstance();
    private final ShellCache shellCache = ShellCache.getInstance();
    private final SshSessionCache sshSessionCache = SshSessionCache.getInstance();
    private final SshSessionFactory sshSessionFactory = SshSessionFactory.factory();

    public BlockingMonitorTerminalHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return MONITOR_TERMINAL;
    }

    @Override
    protected <T> void handleBlocking(Promise<Void> promise, Message<T> message) throws Exception {
        Term.WIDTH = console.getWindowWidth();
        Term.HEIGHT = console.getWindowHeight();

        while (true) {
            monitorTerminalSize();

            monitorSshSession();

            if (StatusCache.STOP_PROGRAM) {
                break;
            }

            Thread.sleep(100);
        }

        promise.complete();
    }

    @Override
    protected <T> void resultBlocking(AsyncResult<Void> asyncResult, Message<T> message) throws Exception {

    }

    private void monitorTerminalSize() {
        int terminalWidth = console.getWindowWidth();
        int terminalHeight = console.getWindowHeight();
        if (terminalWidth < 0 || terminalHeight < 0) {
            return;
        }

        if (Term.WIDTH != terminalWidth || Term.HEIGHT != terminalHeight) {
            Term.WIDTH = terminalWidth;
            Term.HEIGHT = terminalHeight;
            if (Term.status.equals(TermStatus.SHELL)) {
                ShellCache.getInstance().getShell(StatusCache.MONITOR_SESSION_ID).resize(terminalWidth, terminalHeight, StatusCache.MONITOR_SESSION_ID);
            } else if (Term.status.equals(TermStatus.TERMIO)) {
                Term.getInstance().printScene(true);
            }
        }
    }

    private void monitorSshSession() {
        sshSessionCache.getSessionMap().forEach((sessionId, session) -> {
            if (session == null) {
                return;
            }
            if (!session.alive()) {
                if (!sshSessionCache.containSessionId(sessionId)) {
                    return;
                }
                sshSessionCache.stop(sessionId);
                shellCache.initializeQuickSessionSwitchHelper();
            }
        });
    }
}
