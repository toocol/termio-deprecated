package com.toocol.ssh.core.ssh.handlers;

import com.toocol.ssh.core.auth.core.SshCredential;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.ShellCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.shell.core.ShellProtocol;
import com.toocol.ssh.core.ssh.core.SshSessionFactory;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.core.term.core.TermStatus;
import com.toocol.ssh.core.term.handlers.BlockingAcceptCommandHandler;
import com.toocol.ssh.core.term.handlers.BlockingMonitorTerminalHandler;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.functional.Executable;
import com.toocol.ssh.utilities.functional.Ordered;
import com.toocol.ssh.utilities.handler.BlockingMessageHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.toocol.ssh.core.shell.ShellAddress.DISPLAY_SHELL;
import static com.toocol.ssh.core.shell.ShellAddress.RECEIVE_SHELL;
import static com.toocol.ssh.core.ssh.SshAddress.ESTABLISH_SSH_SESSION;
import static com.toocol.ssh.core.term.TermAddress.ACCEPT_COMMAND;

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
        return ESTABLISH_SSH_SESSION;
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

            BlockingMonitorTerminalHandler.sessionId = sessionId;
            Term.status = TermStatus.SHELL;
            eventBus.send(DISPLAY_SHELL.address(), sessionId);
            eventBus.send(RECEIVE_SHELL.address(), sessionId);

        } else {

            warn("Establish ssh connection failed.");
            eventBus.send(ACCEPT_COMMAND.address(), BlockingAcceptCommandHandler.CONNECT_FAILED);

        }
    }

}
