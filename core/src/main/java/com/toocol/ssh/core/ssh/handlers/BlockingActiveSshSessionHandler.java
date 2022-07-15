package com.toocol.ssh.core.ssh.handlers;

import com.toocol.ssh.core.auth.core.SshCredential;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.ShellCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.shell.core.ShellProtocol;
import com.toocol.ssh.core.ssh.core.SshSessionFactory;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.functional.Executable;
import com.toocol.ssh.utilities.functional.Ordered;
import com.toocol.ssh.utilities.handler.BlockingMessageHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.toocol.ssh.core.ssh.SshAddress.ACTIVE_SSH_SESSION;

/**
 * Active an ssh session without enter the Shell.
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 20:49
 * @version: 0.0.1
 */
@Ordered
public final class BlockingActiveSshSessionHandler extends BlockingMessageHandler<JsonObject> {

    private final CredentialCache credentialCache = CredentialCache.getInstance();
    private final ShellCache shellCache = ShellCache.getInstance();
    private final SshSessionCache sshSessionCache = SshSessionCache.getInstance();
    private final SshSessionFactory factory = SshSessionFactory.factory();

    public BlockingActiveSshSessionHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    protected <T> void handleBlocking(Promise<JsonObject> promise, Message<T> message) throws Exception {
        JsonObject ret = new JsonObject();
        JsonArray success = new JsonArray();
        JsonArray failed = new JsonArray();
        JsonArray index = cast(message.body());
        info(index.toString());
        for (Object o : index) {
            SshCredential credential = credentialCache.getCredential(Integer.parseInt(o.toString()));
            assert credential != null;
            try {
                AtomicReference<Long> sessionId = new AtomicReference<>(sshSessionCache.containSession(credential.getHost()));

                Executable execute = () -> {
                    Optional.ofNullable(sshSessionCache.getChannelShell(sessionId.get())).ifPresent(channelShell -> {
                        int width = Term.getInstance().getWidth();
                        int height = Term.getInstance().getHeight();
                        channelShell.setPtySize(width, height, width, height);
                    });

                    System.gc();
                    if (sessionId.get() > 0) {
                        success.add(credential.getHost() + "@" + credential.getUser());
                    } else {
                        failed.add(credential.getHost() + "@" + credential.getUser());
                    }
                };

                if (sessionId.get() == 0) {
                    sessionId.set(factory.createSession(credential));

                    Shell shell = new Shell(sessionId.get(), vertx, eventBus, sshSessionCache.getChannelShell(sessionId.get()));
                    shell.setUser(credential.getUser());
                    shellCache.putShell(sessionId.get(), shell);
                    shell.initialFirstCorrespondence(ShellProtocol.SSH, execute);
                } else {
                    long newSessionId = factory.invokeSession(sessionId.get(), credential);
                    if (newSessionId != sessionId.get() || !shellCache.contains(newSessionId)) {
                        Shell shell = new Shell(sessionId.get(), vertx, eventBus, sshSessionCache.getChannelShell(sessionId.get()));
                        shell.setUser(credential.getUser());
                        shellCache.putShell(sessionId.get(), shell);
                        sessionId.set(newSessionId);
                        shell.initialFirstCorrespondence(ShellProtocol.SSH, execute);
                    } else {
                        shellCache.getShell(sessionId.get()).resetIO(ShellProtocol.SSH);
                        sessionId.set(newSessionId);
                        execute.execute();
                    }
                }
            } catch (Exception e) {
                // do nothing
            }
        }

        ret.put("success", success);
        ret.put("failed", failed);
        promise.complete(ret);
    }

    @Override
    protected <T> void resultBlocking(AsyncResult<JsonObject> asyncResult, Message<T> message) throws Exception {
        if (asyncResult.succeeded()) {
            Term term = Term.getInstance();
            term.printScene(false);
            term.printDisplay(String.valueOf(asyncResult.result()));
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
