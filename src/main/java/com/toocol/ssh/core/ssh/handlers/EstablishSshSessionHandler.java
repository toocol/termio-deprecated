package com.toocol.ssh.core.ssh.handlers;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.router.IAddress;
import com.toocol.ssh.common.utils.SnowflakeGuidGenerator;
import com.toocol.ssh.core.ssh.jsch.SshUserInfo;
import com.toocol.ssh.core.ssh.cache.SessionCache;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import java.util.Properties;

import static com.toocol.ssh.core.ssh.SshVerticleAddress.CONNECT_CHANNEL_SHELL;
import static com.toocol.ssh.core.ssh.SshVerticleAddress.ESTABLISH_SESSION;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
public class EstablishSshSessionHandler extends AbstractMessageHandler<Long> {

    private JSch jSch;
    private SnowflakeGuidGenerator guidGenerator;
    private SessionCache sessionCache;

    public EstablishSshSessionHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress address() {
        return ESTABLISH_SESSION;
    }

    @Override
    protected <T> void handleWithin(Future<Long> future, Message<T> message) throws Exception {
        // TODO: same ip of connection's connection need only create once.
        Session session = jSch.getSession("root", "47.108.157.178", 22);
        session.setPassword("");
        session.setUserInfo(new SshUserInfo());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        int timeout = 30000;
        session.setTimeout(timeout);
        session.connect();

        long sessionId = guidGenerator.nextId();
        sessionCache.putSession(sessionId, session);
        future.complete(sessionId);
    }

    @Override
    protected <T> void resultWithin(AsyncResult<Long> asyncResult, Message<T> message) throws Exception {
        Long sessionId = asyncResult.result();
        eventBus.send(CONNECT_CHANNEL_SHELL.address(), sessionId);
    }

    @SafeVarargs
    @Override
    public final <T> void inject(T... objs) {
        jSch = cast(objs[0]);
        guidGenerator = cast(objs[1]);
        sessionCache = cast(objs[2]);
    }
}
