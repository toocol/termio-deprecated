package com.toocol.termio.console.handlers;

import com.toocol.termio.core.cache.CredentialCache;
import com.toocol.termio.core.cache.ShellCache;
import com.toocol.termio.core.cache.SshSessionCache;
import com.toocol.termio.core.cache.StatusCache;
import com.toocol.termio.core.ssh.core.SshSessionFactory;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.core.term.core.TermStatus;
import com.toocol.termio.utilities.module.IAddress;
import com.toocol.termio.utilities.console.Console;
import com.toocol.termio.utilities.module.BlockingMessageHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.jetbrains.annotations.NotNull;

import static com.toocol.termio.core.term.TermAddress.MONITOR_TERMINAL;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/13 0:58
 * @version: 0.0.1
 */
@SuppressWarnings("all")
public final class BlockingMonitorTerminalHandler extends BlockingMessageHandler<Void> {

    private final Console console = Console.get();
    private final CredentialCache.Instance credentialCache = CredentialCache.Instance;
    private final ShellCache.Instance shellCache = ShellCache.Instance;
    private final SshSessionCache.Instance sshSessionCache = SshSessionCache.Instance;
    private final SshSessionFactory.Instance sshSessionFactory = SshSessionFactory.Instance;

    public BlockingMonitorTerminalHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @NotNull
    @Override
    public IAddress consume() {
        return MONITOR_TERMINAL;
    }

    @Override
    protected <T> void handleBlocking(@NotNull Promise<Void> promise, @NotNull Message<T> message) throws Exception {
        Term.width = console.getWindowWidth();
        Term.height = console.getWindowHeight();

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
    protected <T> void resultBlocking(@NotNull AsyncResult<Void> asyncResult, @NotNull Message<T> message) throws Exception {

    }

    private void monitorTerminalSize() {
        int terminalWidth = console.getWindowWidth();
        int terminalHeight = console.getWindowHeight();
        if (terminalWidth < 0 || terminalHeight < 0) {
            return;
        }

        if (Term.width != terminalWidth || Term.height != terminalHeight) {
            Term.width = terminalWidth;
            Term.height = terminalHeight;
            if (Term.status.equals(TermStatus.SHELL)) {
                ShellCache.Instance.getShell(StatusCache.MONITOR_SESSION_ID).resize(terminalWidth, terminalHeight, StatusCache.MONITOR_SESSION_ID);
            } else if (Term.status.equals(TermStatus.TERMIO)) {
                Term.instance.printScene(true);
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
