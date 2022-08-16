package com.toocol.termio.core.ssh.handlers;

import com.toocol.termio.core.auth.core.SshCredential;
import com.toocol.termio.core.cache.*;
import com.toocol.termio.core.shell.core.Shell;
import com.toocol.termio.core.shell.core.ShellProtocol;
import com.toocol.termio.core.ssh.core.SshSessionFactory;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.core.term.core.TermStatus;
import com.toocol.termio.utilities.address.IAddress;
import com.toocol.termio.utilities.functional.Executable;
import com.toocol.termio.utilities.functional.Ordered;
import com.toocol.termio.utilities.handler.BlockingMessageHandler;
import com.toocol.termio.core.shell.ShellAddress;
import com.toocol.termio.core.ssh.SshAddress;
import com.toocol.termio.core.term.TermAddress;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
@Ordered
public final class BlockingEstablishSshSessionHandler extends BlockingMessageHandler<Long> {

    private final CredentialCache credentialCache = CredentialCache.getInstance();
    private final SshSessionCache sshSessionCache = SshSessionCache.getInstance();
    private final ShellCache shellCache = ShellCache.getInstance();
    private final SshSessionFactory factory = SshSessionFactory.factory();

    public BlockingEstablishSshSessionHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return SshAddress.ESTABLISH_SSH_SESSION;
    }

    @Override
    protected <T> void handleBlocking(Promise<Long> promise, Message<T> message) throws Exception {
        int index = cast(message.body());
        SshCredential credential = credentialCache.getCredential(index);

        try {
            assert credential != null;

            final AtomicReference<Long> sessionId = new AtomicReference<>(sshSessionCache.containSession(credential.getHost()));

            // execute in the final
            Executable execute = () -> {
                Optional.ofNullable(sshSessionCache.getChannelShell(sessionId.get())).ifPresent(channelShell -> {
                    int width = Term.getInstance().getWidth();
                    int height = Term.getInstance().getHeight();
                    channelShell.setPtySize(width, height, width, height);
                });
                StatusCache.HANGED_QUIT = false;
                shellCache.initializeQuickSessionSwitchHelper();

                if (sessionId.get() > 0) {
                    promise.complete(sessionId.get());
                } else {
                    promise.fail("Session establish failed.");
                }
            };

            if (sessionId.get() == 0) {
                StatusCache.HANGED_ENTER = false;
                sessionId.set(factory.createSession(credential));

                Shell shell = new Shell(sessionId.get(), credential.getHost(),credential.getUser(), vertx, eventBus, sshSessionCache.getChannelShell(sessionId.get()));
                shell.setUser(credential.getUser());
                shellCache.putShell(sessionId.get(), shell);
                shell.setJumpServer(credential.isJumpServer());
                shell.initialFirstCorrespondence(ShellProtocol.SSH, execute);
            } else {
                StatusCache.HANGED_ENTER = true;
                long newSessionId = factory.invokeSession(sessionId.get(), credential);

                if (newSessionId != sessionId.get() || !shellCache.contains(newSessionId)) {
                    Shell shell = new Shell(sessionId.get(), credential.getHost(), credential.getUser(), vertx, eventBus, sshSessionCache.getChannelShell(sessionId.get()));
                    shell.setUser(credential.getUser());
                    shellCache.putShell(sessionId.get(), shell);
                    sessionId.set(newSessionId);
                    shell.setJumpServer(credential.isJumpServer());
                    shell.initialFirstCorrespondence(ShellProtocol.SSH, execute);
                } else {
                    Shell shell = shellCache.getShell(newSessionId);
                    if (shell.getChannelShell() == null) {
                        /* If the connection is established through Mosh, it needs to be set the ChannelShell*/
                        shell.setChannelShell(sshSessionCache.getChannelShell(sessionId.get()));
                    }
                    shell.resetIO(ShellProtocol.SSH);
                    sessionId.set(newSessionId);
                    execute.execute();
                }
            }

        } catch (Exception e) {
            promise.complete(null);
        } finally {
            // invoke gc() to clean up already un-use object during initial processing. (it's very efficacious :))
            System.gc();
        }
    }

    @Override
    protected <T> void resultBlocking(AsyncResult<Long> asyncResult, Message<T> message) throws Exception {
        Long sessionId = asyncResult.result();
        if (sessionId != null) {

            shellCache.getShell(sessionId).printAfterEstablish();
            StatusCache.SHOW_WELCOME = true;

            StatusCache.MONITOR_SESSION_ID = sessionId;
            Term.status = TermStatus.SHELL;
            eventBus.send(ShellAddress.DISPLAY_SHELL.address(), sessionId);
            eventBus.send(ShellAddress.RECEIVE_SHELL.address(), sessionId);

        } else {

            warn("Establish ssh connection failed.");
            eventBus.send(TermAddress.ACCEPT_COMMAND_CONSOLE.address(), StatusConstants.CONNECT_FAILED);

        }
    }

}
