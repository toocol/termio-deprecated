package com.toocol.ssh.core.ssh.handlers;

import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.router.IAddress;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.ssh.session.SessionCache;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_ACCEPT_COMMAND;
import static com.toocol.ssh.core.ssh.SshVerticleAddress.ACCEPT_SHELL_CMD;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 15:25
 */
public class AcceptShellCmdHandler extends AbstractMessageHandler<Long> {

    private SessionCache sessionCache;

    public AcceptShellCmdHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress address() {
        return ACCEPT_SHELL_CMD;
    }

    @Override
    protected <T> void handleWithin(Future<Long> future, Message<T> message) throws Exception {
        long sessionId = cast(message.body());
        OutputStream outputStream = sessionCache.getChannelShell(sessionId).getOutputStream();
        while (true) {
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            if ("exit".equals(input)) {
                future.complete(sessionId);
                break;
            } else if ("clear".equals(input)) {
                PrintUtil.clear();
                PrintUtil.printTitle();
            }

            input += "\r\n";
            outputStream.write(input.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
    }

    @Override
    protected <T> void resultWithin(AsyncResult<Long> asyncResult, Message<T> message) throws Exception {
        long sessionId = asyncResult.result();
        sessionCache.getChannelShell(sessionId).disconnect();
        eventBus.send(ADDRESS_ACCEPT_COMMAND.address(), null);
    }

    @SafeVarargs
    @Override
    public final <T> void inject(T... objs) {
        sessionCache = cast(objs[2]);
    }
}
