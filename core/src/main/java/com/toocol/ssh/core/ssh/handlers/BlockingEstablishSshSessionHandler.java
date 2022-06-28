package com.toocol.ssh.core.ssh.handlers;

import com.toocol.ssh.core.auth.core.SshCredential;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.ShellCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.shell.core.ShellProtocol;
import com.toocol.ssh.core.ssh.core.SshSessionFactory;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.core.term.core.TermStatus;
import com.toocol.ssh.core.term.handlers.BlockingAcceptCommandHandler;
import com.toocol.ssh.core.term.handlers.BlockingMonitorTerminalHandler;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.handler.BlockingMessageHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import java.util.Optional;

import static com.toocol.ssh.core.shell.ShellAddress.DISPLAY_SHELL;
import static com.toocol.ssh.core.shell.ShellAddress.RECEIVE_SHELL;
import static com.toocol.ssh.core.ssh.SshAddress.ESTABLISH_SSH_SESSION;
import static com.toocol.ssh.core.term.TermAddress.ACCEPT_COMMAND;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
public final class BlockingEstablishSshSessionHandler extends BlockingMessageHandler<Long> {

    private final SshSessionCache sshSessionCache = SshSessionCache.getInstance();
    private final ShellCache shellCache = ShellCache.getInstance();
    private final SshSessionFactory factory = SshSessionFactory.factory();

    public BlockingEstablishSshSessionHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return ESTABLISH_SSH_SESSION;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<Long> promise, Message<T> message) throws Exception {
        int index = cast(message.body());
        SshCredential credential = CredentialCache.getCredential(index);
        assert credential != null;

        try {
            long sessionId = sshSessionCache.containSession(credential.getHost());

            if (sessionId == 0) {
                StatusCache.HANGED_ENTER = false;
                sessionId = factory.createSession(credential, eventBus);

                Shell shell = new Shell(sessionId, eventBus, sshSessionCache.getChannelShell(sessionId));
                shell.setUser(credential.getUser());
                shell.initialFirstCorrespondence(ShellProtocol.SSH);
                shellCache.putShell(sessionId, shell);
            } else {
                StatusCache.HANGED_ENTER = true;
                long newSessionId = factory.invokeSession(sessionId, credential, eventBus);

                if (newSessionId != sessionId || !shellCache.contains(newSessionId)) {
                    Shell shell = new Shell(sessionId, eventBus, sshSessionCache.getChannelShell(sessionId));
                    shell.setUser(credential.getUser());
                    shell.initialFirstCorrespondence(ShellProtocol.SSH);
                    shellCache.putShell(sessionId, shell);
                    warn("Invoke session failed, re-establish ssh session, sessionId = {}, host = {}, user = {}",
                            sessionId, credential.getHost(), credential.getUser());
                } else {
                    info("Multiplexing ssh session, sessionId = {}, host = {}, user = {}",
                            sessionId, credential.getHost(), credential.getUser());
                    shellCache.getShell(newSessionId).resetIO(ShellProtocol.SSH);
                }
                sessionId = newSessionId;
            }


            Optional.ofNullable(sshSessionCache.getChannelShell(sessionId)).ifPresent(channelShell -> {
                int width = Term.getInstance().getWidth();
                int height = Term.getInstance().getHeight();
                channelShell.setPtySize(width, height, width, height);
            });
            StatusCache.HANGED_QUIT = false;

            // invoke gc() to clean up already un-use object during initial processing. (it's very efficacious :))
            System.gc();
            if (sessionId > 0) {
                promise.complete(sessionId);
            } else {
                promise.fail("Session establish failed.");
            }
        } catch (Exception e) {
            promise.complete(null);
        }
    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<Long> asyncResult, Message<T> message) throws Exception {
        Long sessionId = asyncResult.result();
        if (sessionId != null) {

            Printer.clear();

            if (StatusCache.HANGED_ENTER) {
                Printer.println("Invoke hanged session.\n");
            } else {
                Printer.println("Session established.\n");
            }

            StatusCache.SHOW_WELCOME = true;

            BlockingMonitorTerminalHandler.sessionId = sessionId;
            Term.status = TermStatus.SHELL;
            eventBus.send(DISPLAY_SHELL.address(), sessionId);
            eventBus.send(RECEIVE_SHELL.address(), sessionId);

        } else {

            eventBus.send(ACCEPT_COMMAND.address(), BlockingAcceptCommandHandler.CONNECT_FAILED);

        }
    }

}
