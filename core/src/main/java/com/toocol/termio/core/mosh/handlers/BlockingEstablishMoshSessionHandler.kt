package com.toocol.termio.core.mosh.handlers;

import com.toocol.termio.core.auth.core.SshCredential;
import com.toocol.termio.core.cache.*;
import com.toocol.termio.core.mosh.core.MoshSession;
import com.toocol.termio.core.mosh.core.MoshSessionFactory;
import com.toocol.termio.core.shell.core.Shell;
import com.toocol.termio.core.shell.core.ShellProtocol;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.core.term.core.TermStatus;
import com.toocol.termio.utilities.module.IAddress;
import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.functional.Ordered;
import com.toocol.termio.utilities.module.BlockingMessageHandler;
import com.toocol.termio.utilities.utils.MessageBox;
import com.toocol.termio.core.shell.ShellAddress;
import com.toocol.termio.core.term.TermAddress;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.jetbrains.annotations.NotNull;

import static com.toocol.termio.core.mosh.MoshAddress.*;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/28 23:44
 * @version: 0.0.1
 */
@Ordered
public final class BlockingEstablishMoshSessionHandler extends BlockingMessageHandler<Long> {

    private final SshSessionCache.Instance sshSessionCache = SshSessionCache.Instance;
    private final CredentialCache.Instance credentialCache = CredentialCache.Instance;
    private final MoshSessionFactory moshSessionFactory = MoshSessionFactory.factory(vertx);
    private final ShellCache.Instance shellCache = ShellCache.Instance;

    public BlockingEstablishMoshSessionHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    protected <T> void handleBlocking(@NotNull Promise<Long> promise, @NotNull Message<T> message) throws Exception {
        int index = cast(message.body());
        SshCredential credential = credentialCache.getCredential(index);
        if (credential == null) {
            promise.fail("Credential not exist.");
            return;
        }
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
                    shell.user = credential.getUser();
                    shell.initialFirstCorrespondence(ShellProtocol.MOSH, () -> {
                        shellCache.putShell(sessionId, shell);
                        shell.setChannelShell(sshSessionCache.getChannelShell(sessionId));
                        shellCache.initializeQuickSessionSwitchHelper();

                        Printer.clear();
                        StatusCache.SHOW_WELCOME = true;
                        StatusCache.HANGED_QUIT = false;
                        StatusCache.MONITOR_SESSION_ID = sessionId;

                        Term.status = TermStatus.SHELL;

                        eventBus.send(ShellAddress.DISPLAY_SHELL.address(), sessionId);
                        eventBus.send(ShellAddress.RECEIVE_SHELL.address(), sessionId);

                        System.gc();
                    });
                } catch (Exception e) {
                    eventBus.send(TermAddress.ACCEPT_COMMAND.address(), StatusConstants.CONNECT_FAILED);
                }
            } else {
                eventBus.send(TermAddress.ACCEPT_COMMAND.address(), StatusConstants.CONNECT_FAILED);
            }
        });
        promise.complete();
    }

    @Override
    protected <T> void resultBlocking(@NotNull AsyncResult<Long> asyncResult, @NotNull Message<T> message) throws Exception {
        if (!asyncResult.succeeded()) {
            warn("Establish mosh connection failed.");
            MessageBox.setErrorMessage("Can't touch the mosh-server.");
            eventBus.send(TermAddress.ACCEPT_COMMAND.address(), StatusConstants.NORMAL_BACK);
        }
    }

    @NotNull
    @Override
    public IAddress consume() {
        return ESTABLISH_MOSH_SESSION;
    }
}
