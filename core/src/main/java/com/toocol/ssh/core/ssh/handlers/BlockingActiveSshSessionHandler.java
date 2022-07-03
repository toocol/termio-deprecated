package com.toocol.ssh.core.ssh.handlers;

import com.toocol.ssh.core.auth.core.SshCredential;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.ShellCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.shell.core.ShellProtocol;
import com.toocol.ssh.core.ssh.core.SshSessionFactory;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.handler.BlockingMessageHandler;
import com.toocol.ssh.utilities.status.StatusCache;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.Optional;

import static com.toocol.ssh.core.ssh.SshAddress.ACTIVE_SSH_SESSION;

/**
 * Active an ssh session without enter the Shell.
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 20:49
 * @version: 0.0.1
 */
public final class BlockingActiveSshSessionHandler extends BlockingMessageHandler<Void> {

    private final CredentialCache credentialCache = CredentialCache.getInstance();
    private final ShellCache shellCache = ShellCache.getInstance();
    private final SshSessionCache sshSessionCache = SshSessionCache.getInstance();
    private final SshSessionFactory factory = SshSessionFactory.factory();

    public BlockingActiveSshSessionHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    protected <T> void handleBlocking(Promise<Void> promise, Message<T> message) throws Exception {
        int index = cast(message.body());
        SshCredential credential = credentialCache.getCredential(index);
        assert credential != null;

        try {
            long sessionId = sshSessionCache.containSession(credential.getHost());

            if (sessionId == 0) {
                sessionId = factory.createSession(credential);

                Shell shell = new Shell(sessionId, eventBus, sshSessionCache.getChannelShell(sessionId));
                shell.setUser(credential.getUser());
                shell.initialFirstCorrespondence(ShellProtocol.SSH);
                shellCache.putShell(sessionId, shell);
            } else {
              long newSessionId = factory.invokeSession(sessionId, credential);

                if (newSessionId != sessionId || !shellCache.contains(newSessionId)) {
                    Shell shell = new Shell(sessionId, eventBus, sshSessionCache.getChannelShell(sessionId));
                    shell.setUser(credential.getUser());
                    shell.initialFirstCorrespondence(ShellProtocol.SSH);
                    shellCache.putShell(sessionId, shell);
                } else {
                    shellCache.getShell(sessionId).resetIO(ShellProtocol.SSH);
                }
                sessionId = newSessionId;
            }

            Optional.ofNullable(sshSessionCache.getChannelShell(sessionId)).ifPresent(channelShell -> {
                int width = Term.getInstance().getWidth();
                int height = Term.getInstance().getHeight();
                channelShell.setPtySize(width, height, width, height);
            });

            // invoke gc() to clean up already un-use object during initial processing. (it's very efficacious :))
            System.gc();
            if (sessionId > 0) {
                promise.complete();
            } else {
                promise.fail("Session establish failed.");
            }
        } catch (Exception e) {
            promise.complete(null);
        }
    }

    @Override
    protected <T> void resultBlocking(AsyncResult<Void> asyncResult, Message<T> message) throws Exception {
        if (asyncResult.succeeded()) {
            Term.getInstance().printScene(false);
            message.reply(true);
        } else {
            message.reply(false);
        }
    }

    @Override
    public IAddress consume() {
        return ACTIVE_SSH_SESSION;
    }
}
