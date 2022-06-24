package com.toocol.ssh.core.mosh.handlers;

import com.toocol.ssh.core.auth.core.SshCredential;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.ShellCache;
import com.toocol.ssh.core.mosh.core.MoshSession;
import com.toocol.ssh.core.mosh.core.MoshSessionFactory;
import com.toocol.ssh.core.mosh.core.statesnyc.UserEvent;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.shell.core.ShellProtocol;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.core.term.handlers.BlockingAcceptCommandHandler;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.handler.AbstractBlockingMessageHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.mosh.MoshAddress.*;
import static com.toocol.ssh.core.shell.ShellAddress.DISPLAY_SHELL;
import static com.toocol.ssh.core.shell.ShellAddress.RECEIVE_SHELL;
import static com.toocol.ssh.core.term.TermAddress.ACCEPT_COMMAND;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/28 23:44
 * @version: 0.0.1
 */
public final class BlockingEstablishMoshSessionHandler extends AbstractBlockingMessageHandler<Long> {

    private final MoshSessionFactory moshSessionFactory = MoshSessionFactory.factory(vertx);
    private final ShellCache shellCache = ShellCache.getInstance();

    public BlockingEstablishMoshSessionHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<Long> promise, Message<T> message) throws Exception {
        int index = cast(message.body());
        SshCredential credential = CredentialCache.getCredential(index);
        MoshSession session = moshSessionFactory.getSession(credential);
        if (session == null) {
            promise.complete();
            return;
        }

        long sessionId = session.getSessionId();

        // let event loop thread pool to handler udp packet receive.
        eventBus.request(LISTEN_LOCAL_SOCKET.address(), sessionId, result -> {
            if (result.succeeded()) {
                try {
                    // tell the server the size of the terminal
                    session.resize(new UserEvent.Resize(Term.WIDTH, Term.HEIGHT));

                    eventBus.send(MOSH_TICK.address(), sessionId);

                    Shell shell = new Shell(sessionId, eventBus, session);
                    shell.setUser(credential.getUser());
                    shell.initialFirstCorrespondence(ShellProtocol.MOSH);
                    shellCache.putShell(sessionId, shell);

                    eventBus.send(DISPLAY_SHELL.address(), sessionId);
                    eventBus.send(RECEIVE_SHELL.address(), sessionId);
                } catch (Exception e) {
                    eventBus.send(ACCEPT_COMMAND.address(), BlockingAcceptCommandHandler.CONNECT_FAILED);
                }
            } else {
                eventBus.send(ACCEPT_COMMAND.address(), BlockingAcceptCommandHandler.CONNECT_FAILED);
            }
            System.gc();
        });

        promise.complete();
    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<Long> asyncResult, Message<T> message) throws Exception {

    }

    @Override
    public IAddress consume() {
        return ESTABLISH_MOSH_SESSION;
    }
}
