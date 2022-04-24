package com.toocol.ssh.core.ssh.handlers;

import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.handler.AbstractBlockingMessageHandler;
import com.toocol.ssh.core.auth.vo.SshCredential;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.ssh.core.SshSessionFactory;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.core.term.core.TermStatus;
import com.toocol.ssh.core.term.handlers.BlockingAcceptCommandHandler;
import com.toocol.ssh.core.term.handlers.BlockingMonitorTerminalHandler;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.shell.ShellAddress.*;
import static com.toocol.ssh.core.ssh.SshAddress.ESTABLISH_SSH_SESSION;
import static com.toocol.ssh.core.term.TermAddress.ACCEPT_COMMAND;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
public final class BlockingEstablishSessionHandler extends AbstractBlockingMessageHandler<Long> {

    private final SessionCache sessionCache = SessionCache.getInstance();
    private final SshSessionFactory factory = SshSessionFactory.factory();

    public BlockingEstablishSessionHandler(Vertx vertx, Context context, boolean parallel) {
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

        long sessionId = sessionCache.containSession(credential.getHost());

        if (sessionId == 0) {
            StatusCache.HANGED_ENTER = false;
            sessionId = factory.createSession(credential, eventBus);
        } else {
            sessionId = factory.invokeSession(sessionId, credential, eventBus);
            StatusCache.HANGED_ENTER = true;
        }
        StatusCache.HANGED_QUIT = false;

        // invoke gc() to clean up already un-use object during initial processing. (it's very efficacious :))
        System.gc();
        if (sessionId > 0) {
            promise.complete(sessionId);
        } else {
            promise.fail("Session establish failed.");
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
