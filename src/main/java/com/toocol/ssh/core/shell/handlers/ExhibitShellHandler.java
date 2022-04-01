package com.toocol.ssh.core.shell.handlers;

import com.jcraft.jsch.ChannelShell;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.core.cache.Cache;
import com.toocol.ssh.core.cache.SessionCache;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import java.io.InputStream;

import static com.toocol.ssh.core.shell.ShellVerticleAddress.EXHIBIT_SHELL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 15:44
 */
public class ExhibitShellHandler extends AbstractMessageHandler<Void> {

    private SessionCache sessionCache;

    public ExhibitShellHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress consume() {
        return EXHIBIT_SHELL;
    }

    @Override
    protected <T> void handleWithin(Future<Void> future, Message<T> message) throws Exception {
        long sessionId = cast(message.body());

        ChannelShell channelShell = sessionCache.getChannelShell(sessionId);

        //从远程端到达的所有数据都能从这个流中读取到
        InputStream in = channelShell.getInputStream();
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                String echo = new String(tmp, 0, i);
                if (Cache.CURRENT_COMMAND.equals(echo)) {
                    continue;
                } else if (echo.startsWith(Cache.CURRENT_COMMAND)) {
                    // cd command's echo is like this: cd /\r\n[host@user address]
                    echo = echo.substring(Cache.CURRENT_COMMAND.length());
                }
                Printer.print(echo);
            }

            if (Cache.HANGED_QUIT) {
                if (in.available() > 0) {
                    continue;
                }
                Printer.println("hang up connection.");
                break;
            }
            if (channelShell.isClosed()) {
                if (in.available() > 0) {
                    continue;
                }
                Printer.println("exit-status: " + channelShell.getExitStatus());
                break;
            }
        }
        future.complete();
    }

    @Override
    protected <T> void resultWithin(AsyncResult<Void> asyncResult, Message<T> message) throws Exception {
        if (Cache.HANGED_QUIT) {
            Cache.HANGED_QUIT = false;
            Cache.HANGED_ENTER = true;
        }
    }

    @SafeVarargs
    @Override
    public final <T> void inject(T... objs) {
        sessionCache = cast(objs[2]);
    }
}
