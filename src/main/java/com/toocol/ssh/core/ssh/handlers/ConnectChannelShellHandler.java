package com.toocol.ssh.core.ssh.handlers;

import com.jcraft.jsch.ChannelShell;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.router.IAddress;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.ssh.cache.SessionCache;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.ssh.SshVerticleAddress.*;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 13:40
 */
public class ConnectChannelShellHandler extends AbstractMessageHandler<Long> {

    private SessionCache sessionCache;

    public ConnectChannelShellHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress address() {
        return CONNECT_CHANNEL_SHELL;
    }

    @Override
    protected <T> void handleWithin(Future<Long> future, Message<T> message) throws Exception {
        PrintUtil.clear();
        PrintUtil.printTitle();

        long sessionId = cast(message.body());
        System.out.println("Session established");

        ChannelShell channelShell = cast(sessionCache.getSession(sessionId).openChannel("shell"));
        channelShell.connect();
        sessionCache.putChannelShell(sessionId, channelShell);

        future.complete(sessionId);
    }

    @Override
    protected <T> void resultWithin(AsyncResult<Long> asyncResult, Message<T> message) throws Exception {
        eventBus.send(EXHIBIT_SHELL.address(), asyncResult.result());
        eventBus.send(ACCEPT_SHELL_CMD.address(), asyncResult.result());
    }

    @SafeVarargs
    @Override
    public final <T> void inject(T... objs) {
        sessionCache = cast(objs[2]);
    }
}
