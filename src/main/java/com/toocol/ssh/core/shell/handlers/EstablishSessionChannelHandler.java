package com.toocol.ssh.core.shell.handlers;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.common.utils.SnowflakeGuidGenerator;
import com.toocol.ssh.core.cache.Cache;
import com.toocol.ssh.core.credentials.vo.SshCredential;
import com.toocol.ssh.core.shell.vo.SshUserInfo;
import com.toocol.ssh.core.cache.SessionCache;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import java.util.Properties;

import static com.toocol.ssh.core.shell.ShellVerticleAddress.*;
import static com.toocol.ssh.core.shell.ShellVerticleAddress.ACCEPT_SHELL_CMD;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
public class EstablishSessionChannelHandler extends AbstractMessageHandler<Long> {

    private JSch jSch;
    private SnowflakeGuidGenerator guidGenerator;
    private SessionCache sessionCache;

    public EstablishSessionChannelHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress consume() {
        return ESTABLISH_SESSION;
    }

    @Override
    protected <T> void handleWithin(Future<Long> future, Message<T> message) throws Exception {
        int index = cast(message.body());
        SshCredential credential = Cache.getCredential(index);
        assert credential != null;

        long sessionId = sessionCache.containSession(credential.getHost());
        if (sessionId == 0) {
            Session session = jSch.getSession(credential.getUser(), credential.getHost(), credential.getPort());
            session.setPassword(credential.getPassword());
            session.setUserInfo(new SshUserInfo());
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            int timeout = 30000;
            session.setTimeout(timeout);
            session.connect();

            sessionId = guidGenerator.nextId();
            sessionCache.putSession(sessionId, session);

            ChannelShell channelShell = cast(session.openChannel("shell"));
            channelShell.connect();
            sessionCache.putChannelShell(sessionId, channelShell);
        }

        future.complete(sessionId);
    }

    @Override
    protected <T> void resultWithin(AsyncResult<Long> asyncResult, Message<T> message) throws Exception {
        Printer.clear();
        if (Cache.HANGED_ENTER) {
            Printer.println("Invoke hanged session.");
        } else {
            Printer.println("Session established.\n");
        }

        Long sessionId = asyncResult.result();
        eventBus.send(EXHIBIT_SHELL.address(), sessionId);
        eventBus.send(ACCEPT_SHELL_CMD.address(), sessionId);
    }

    @SafeVarargs
    @Override
    public final <T> void inject(T... objs) {
        jSch = cast(objs[0]);
        guidGenerator = cast(objs[1]);
        sessionCache = cast(objs[2]);
    }
}
