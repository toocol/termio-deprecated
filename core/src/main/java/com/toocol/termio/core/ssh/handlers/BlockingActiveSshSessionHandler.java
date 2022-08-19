package com.toocol.termio.core.ssh.handlers;

import com.toocol.termio.core.auth.core.SshCredential;
import com.toocol.termio.core.cache.CredentialCache;
import com.toocol.termio.core.cache.ShellCache;
import com.toocol.termio.core.cache.SshSessionCache;
import com.toocol.termio.core.shell.core.Shell;
import com.toocol.termio.core.shell.core.ShellProtocol;
import com.toocol.termio.core.ssh.SshAddress;
import com.toocol.termio.core.ssh.core.SshSessionFactory;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.utilities.module.IAddress;
import com.toocol.termio.utilities.ansi.AnsiStringBuilder;
import com.toocol.termio.utilities.functional.Executable;
import com.toocol.termio.utilities.functional.Ordered;
import com.toocol.termio.utilities.module.BlockingMessageHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Active an ssh session without enter the Shell.
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 20:49
 * @version: 0.0.1
 */
@Ordered
public final class BlockingActiveSshSessionHandler extends BlockingMessageHandler<JsonObject> {

    private final CredentialCache.Instance credentialCache = CredentialCache.Instance;
    private final ShellCache.Instance shellCache = ShellCache.Instance;
    private final SshSessionCache.Instance sshSessionCache = SshSessionCache.Instance;
    private final SshSessionFactory.Instance factory = SshSessionFactory.Instance;

    public BlockingActiveSshSessionHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    protected <T> void handleBlocking(Promise<JsonObject> promise, Message<T> message) throws Exception {
        JsonObject ret = new JsonObject();
        JsonArray success = new JsonArray();
        JsonArray failed = new JsonArray();
        JsonArray index = cast(message.body());
        AtomicInteger rec = new AtomicInteger();

        for (int i = 0; i < index.size(); i++) {
            SshCredential credential = credentialCache.getCredential(index.getInteger(i));
            assert credential != null;
            try {
                AtomicReference<Long> sessionId = new AtomicReference<>(sshSessionCache.containSession(credential.getHost()));

                Executable execute = () -> {
                    Optional.ofNullable(sshSessionCache.getChannelShell(sessionId.get())).ifPresent(channelShell -> {
                        int width = Term.width;
                        int height = Term.height;
                        channelShell.setPtySize(width, height, width, height);
                    });

                    System.gc();
                    if (sessionId.get() > 0) {
                        success.add(credential.getHost() + "@" + credential.getUser());
                    } else {
                        failed.add(credential.getHost() + "@" + credential.getUser());
                    }

                    if (rec.incrementAndGet() == index.size()) {
                        shellCache.initializeQuickSessionSwitchHelper();
                        ret.put("success", success);
                        ret.put("failed", failed);
                        promise.complete(ret);
                    }
                };

                if (sessionId.get() == 0) {
                    sessionId.set(factory.createSession(credential));
                    Shell shell = new Shell(sessionId.get(), credential.getHost(), credential.getUser(), vertx, eventBus, sshSessionCache.getChannelShell(sessionId.get()));
                    shell.user = credential.getUser();
                    shellCache.putShell(sessionId.get(), shell);
                    shell.initialFirstCorrespondence(ShellProtocol.SSH, execute);
                } else {
                    long newSessionId = factory.invokeSession(sessionId.get(), credential);
                    if (newSessionId != sessionId.get() || !shellCache.contains(newSessionId)) {
                        Shell shell = new Shell(sessionId.get(), credential.getHost(), credential.getUser(), vertx, eventBus, sshSessionCache.getChannelShell(sessionId.get()));
                        shell.user = credential.getUser();
                        shellCache.putShell(sessionId.get(), shell);
                        sessionId.set(newSessionId);
                        shell.initialFirstCorrespondence(ShellProtocol.SSH, execute);
                    } else {
                        Shell shell = shellCache.getShell(sessionId.get());
                        if (shell == null) {
                            throw new RuntimeException();
                        }
                        shell.resetIO(ShellProtocol.SSH);
                        sessionId.set(newSessionId);
                        execute.execute();
                    }
                }
            } catch (Exception e) {
                failed.add(credential.getHost() + "@" + credential.getUser());
                if (rec.incrementAndGet() == index.size()) {
                    ret.put("success", success);
                    ret.put("failed", failed);
                    promise.complete(ret);
                }

            }
        }
    }

    @Override
    protected <T> void resultBlocking(AsyncResult<JsonObject> asyncResult, Message<T> message) throws Exception {

        if (asyncResult.succeeded()) {
            Term term = Term.instance;
            term.printScene(false);
            JsonObject activeMsg = asyncResult.result();
            AnsiStringBuilder ansiStringBuilder = new AnsiStringBuilder();
            int width = Term.width;
            for (Map.Entry<String, Object> stringObjectEntry : activeMsg) {
                if ("success".equals(stringObjectEntry.getKey())) {
                    ansiStringBuilder.append(stringObjectEntry.getKey() + ":" + "\n");
                    String value = stringObjectEntry.getValue().toString();
                    String[] split = value.replace("[", "").replace("]", "").replace("\"", "").split(",");
                    for (int i = 0; i < split.length; i++) {
                        if (width < 24 * 3) {
                            if (i != 0 & i % 2 == 0) {
                                ansiStringBuilder.append("\n");
                            }
                        } else {
                            if (i != 0 & i % 3 == 0) {
                                ansiStringBuilder.append("\n");
                            }
                        }
                        ansiStringBuilder.front(Term.theme.activeSuccessMsgColor.color)
                                .background(Term.theme.displayBackGroundColor.color)
                                .append(split[i] + StringUtils.repeat(" ",4));
                    }
                } else {
                    ansiStringBuilder.deFront().append("\n" + stringObjectEntry.getKey() + ":" + "\n");
                    String value = stringObjectEntry.getValue().toString();
                    String[] split = value.replace("[", "").replace("]", "").replace("\"", "").split(",");
                    for (int j = 0; j < split.length; j++) {
                        if (width < 24 * 3) {
                            if (j != 0 & j % 2 == 0) {
                                ansiStringBuilder.append("\n");
                            }
                        } else {
                            if (j != 0 && j % 3 == 0) {
                                ansiStringBuilder.append("\n");
                            }
                        }
                        ansiStringBuilder.front(Term.theme.activeFailedMsgColor.color)
                                .background(Term.theme.displayBackGroundColor.color)
                                .append(split[j] + StringUtils.repeat(" ",4));
                    }

                }
            }
            term.printDisplay(ansiStringBuilder.toString());
            message.reply(true);
        } else {
            message.reply(false);
        }

    }

    @Override
    public IAddress consume() {
        return SshAddress.ACTIVE_SSH_SESSION;
    }
}
