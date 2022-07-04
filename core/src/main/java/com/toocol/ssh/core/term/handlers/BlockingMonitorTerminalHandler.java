package com.toocol.ssh.core.term.handlers;

import com.toocol.ssh.core.auth.core.SshCredential;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.ShellCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.shell.core.ShellProtocol;
import com.toocol.ssh.core.ssh.core.SshSessionFactory;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.core.term.core.TermStatus;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.console.Console;
import com.toocol.ssh.utilities.handler.BlockingMessageHandler;
import com.toocol.ssh.utilities.status.StatusCache;
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
            session = sshSessionCache.getSession(sessionId);
            if (session == null) {
                return;
            }
            if (!session.isConnected()) {
                if (!sshSessionCache.containSessionId(sessionId)) {
                    return;
                }
                warn("SessionId = {}, this SSH connection is detected to be disconnected, try to re-connect again", sessionId);
                try {
                    SshCredential credential = credentialCache.getCredential(session.getHost());
                    long newSessionId = sshSessionFactory.invokeSession(sessionId, credential);
                    Shell shell = new Shell(newSessionId, eventBus, sshSessionCache.getChannelShell(newSessionId));
                    shell.setUser(credential.getUser());
                    shell.initialFirstCorrespondence(ShellProtocol.SSH);

                    shellCache.putShell(newSessionId, shell);
                    sshSessionCache.stop(sessionId);
                    shellCache.stop(sessionId);

                    info("Re-connect session success, newSessionId = {}, host = {}, " +
                            "old session is destroy, destroySessionId = {}", newSessionId, credential.getHost(), sessionId);
                } catch (Exception e) {
                    error("Re-connect session failed, remove this SSH session, stackTrace : {}", parseStackTrace(e));
                }
            }
        });
    }
}
