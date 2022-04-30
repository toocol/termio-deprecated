package com.toocol.ssh.core.mosh.handlers;

import com.toocol.ssh.core.auth.core.SshCredential;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.ShellCache;
import com.toocol.ssh.core.mosh.core.MoshSession;
import com.toocol.ssh.core.mosh.core.MoshSessionFactory;
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

import static com.toocol.ssh.core.mosh.MoshAddress.ESTABLISH_MOSH_SESSION;
import static com.toocol.ssh.core.mosh.MoshAddress.LISTEN_LOCAL_SOCKET;
import static com.toocol.ssh.core.shell.ShellAddress.DISPLAY_SHELL;
import static com.toocol.ssh.core.shell.ShellAddress.RECEIVE_SHELL;
import static com.toocol.ssh.core.term.TermAddress.ACCEPT_COMMAND;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/28 23:44
 * @version: 0.0.1
 */
public class BlockingEstablishMoshSessionHandler extends AbstractBlockingMessageHandler<Long> {

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
        long sessionId = session.getSessionId();

        // let event loop thread pool to handler udp packet receive.
        vertx.eventBus().request(LISTEN_LOCAL_SOCKET.address(), sessionId, messageAsyncResult -> {
            try {
                Shell shell = new Shell(sessionId, eventBus, session);
                shell.setUser(credential.getUser());
                shell.resetIO(ShellProtocol.MOSH);
                shell.initialFirstCorrespondence(ShellProtocol.MOSH);
                shellCache.putShell(sessionId, shell);

                eventBus.send(DISPLAY_SHELL.address(), sessionId);
                eventBus.send(RECEIVE_SHELL.address(), sessionId);
            } catch (Exception e) {
                eventBus.send(ACCEPT_COMMAND.address(), BlockingAcceptCommandHandler.CONNECT_FAILED);
            }
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
