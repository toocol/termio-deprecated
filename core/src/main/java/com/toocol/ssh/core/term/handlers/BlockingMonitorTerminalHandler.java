package com.toocol.ssh.core.term.handlers;

import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.ShellCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.ssh.core.SshSessionFactory;
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

    public static volatile long sessionId;

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
                ShellCache.getInstance().getShell(sessionId).resize(terminalWidth, terminalHeight, sessionId);
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
            }
        });
    }
}
