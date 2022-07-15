package com.toocol.ssh.core.mosh.handlers;

import com.toocol.ssh.core.auth.core.SshCredential;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.ShellCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.mosh.core.MoshSession;
import com.toocol.ssh.core.mosh.core.MoshSessionFactory;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.shell.core.ShellProtocol;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.core.term.core.TermStatus;
import com.toocol.ssh.core.term.handlers.BlockingAcceptCommandHandler;
import com.toocol.ssh.core.term.handlers.BlockingMonitorTerminalHandler;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.annotation.Order;
import com.toocol.ssh.utilities.handler.BlockingMessageHandler;
import com.toocol.ssh.utilities.utils.MessageBox;
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
@Order
public final class BlockingEstablishMoshSessionHandler extends BlockingMessageHandler<Long> {

    private final CredentialCache credentialCache = CredentialCache.getInstance();
    private final MoshSessionFactory moshSessionFactory = MoshSessionFactory.factory(vertx);
    private final ShellCache shellCache = ShellCache.getInstance();

    public BlockingEstablishMoshSessionHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    protected <T> void handleBlocking(Promise<Long> promise, Message<T> message) throws Exception {
        int index = cast(message.body());
        SshCredential credential = credentialCache.getCredential(index);
        MoshSession session = moshSessionFactory.getSession(credential);
        if (session == null) {
            promise.fail("Can't touch the mosh-server.");
            return;
        }

        long sessionId = session.getSessionId();

        // let event loop thread pool to handler udp packet receive.
        eventBus.request(LISTEN_LOCAL_SOCKET.address(), sessionId, result -> {
            if (result.succeeded()) {
                try {
                    eventBus.send(MOSH_TICK.address(), sessionId);

                    Shell shell = new Shell(sessionId, vertx, eventBus, session);
                    shell.setUser(credential.getUser());
                    shell.initialFirstCorrespondence(ShellProtocol.MOSH);
                    shellCache.putShell(sessionId, shell);
                    shell.printAfterEstablish();

                    StatusCache.SHOW_WELCOME = true;
                    StatusCache.HANGED_QUIT = false;

                    BlockingMonitorTerminalHandler.sessionId = sessionId;
                    Term.status = TermStatus.SHELL;

                    eventBus.send(DISPLAY_SHELL.address(), sessionId);
                    eventBus.send(RECEIVE_SHELL.address(), sessionId);
                } catch (Exception e) {
                    eventBus.send(ACCEPT_COMMAND.address(), BlockingAcceptCommandHandler.CONNECT_FAILED);
                } finally {
                    System.gc();
                }
            } else {
                eventBus.send(ACCEPT_COMMAND.address(), BlockingAcceptCommandHandler.CONNECT_FAILED);
            }
        });
        promise.complete();
    }

    @Override
    protected <T> void resultBlocking(AsyncResult<Long> asyncResult, Message<T> message) throws Exception {
        if (!asyncResult.succeeded()) {
            warn("Establish mosh connection failed.");
            MessageBox.setErrorMessage("Can't touch the mosh-server.");
            eventBus.send(ACCEPT_COMMAND.address(), BlockingAcceptCommandHandler.NORMAL_BACK);
        }
    }

    @Override
    public IAddress consume() {
        return ESTABLISH_MOSH_SESSION;
    }
}
