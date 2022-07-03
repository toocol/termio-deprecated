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
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.handler.BlockingMessageHandler;
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
public final class BlockingActiveSshSessionHandler extends BlockingMessageHandler<JsonObject> {

    private final ShellCache shellCache = ShellCache.getInstance();
    private final SshSessionCache sshSessionCache = SshSessionCache.getInstance();
    private final SshSessionFactory factory = SshSessionFactory.factory();

    public BlockingActiveSshSessionHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    protected <T> void handleBlocking(Promise<JsonObject> promise, Message<T> message) throws Exception {
        int index = cast(message.body());
        SshCredential credential = CredentialCache.getCredential(index);
        assert credential != null;

        try {
            long sessionId = sshSessionCache.containSession(credential.getHost());
//
//            if (sessionId == 0) {
//                StatusCache.HANGED_ENTER = false;
//                sessionId = factory.createSession(credential, eventBus);
//
//                Shell shell = new Shell(sessionId, eventBus, sshSessionCache.getChannelShell(sessionId));
//                shell.setUser(credential.getUser());
//                shell.initialFirstCorrespondence(ShellProtocol.SSH);
//                shellCache.putShell(sessionId, shell);
//            } else {
                StatusCache.HANGED_ENTER = true;
              /*  long newSessionId = factory.invokeSession(sessionId, credential, eventBus);

                if (newSessionId != sessionId || !shellCache.contains(newSessionId)) {
                    Shell shell = new Shell(sessionId, eventBus, sshSessionCache.getChannelShell(sessionId));
                    shell.setUser(credential.getUser());
                    shell.initialFirstCorrespondence(ShellProtocol.SSH);
                    shellCache.putShell(sessionId, shell);
                } else {*/
                    shellCache.getShell(sessionId).resetIO(ShellProtocol.SSH);
//                }
//                sessionId = newSessionId;
//            }


            Optional.ofNullable(sshSessionCache.getChannelShell(sessionId)).ifPresent(channelShell -> {
                int width = Term.getInstance().getWidth();
                int height = Term.getInstance().getHeight();
                channelShell.setPtySize(width, height, width, height);
            });
            StatusCache.HANGED_QUIT = false;

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

        //TODO: see com.toocol.ssh.core.ssh.handlers.BlockingEstablishSessionHandler
    }

    @Override
    protected <T> void resultBlocking(AsyncResult<JsonObject> asyncResult, Message<T> message) throws Exception {
        if (asyncResult.succeeded()) {
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
