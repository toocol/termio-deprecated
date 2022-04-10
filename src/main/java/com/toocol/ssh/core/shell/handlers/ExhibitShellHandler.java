package com.toocol.ssh.core.shell.handlers;

import com.jcraft.jsch.ChannelShell;
import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.core.cache.Cache;
import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.shell.core.Shell;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;

import java.io.InputStream;

import static com.toocol.ssh.core.shell.ShellVerticleAddress.EXHIBIT_SHELL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 15:44
 */
public class ExhibitShellHandler extends AbstractMessageHandler<Void> {

    private final SessionCache sessionCache = SessionCache.getInstance();

    public ExhibitShellHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress consume() {
        return EXHIBIT_SHELL;
    }

    @Override
    protected <T> void handleWithin(Promise<Void> promise, Message<T> message) throws Exception {
        long sessionId = cast(message.body());

        ChannelShell channelShell = sessionCache.getChannelShell(sessionId);
        Shell shell = sessionCache.getShell(sessionId);

        if (shell.getWelcome() != null) {
            Printer.print(shell.getWelcome());
        }

        Printer.print(shell.getPrompt());

        /*
        * All the remote feedback data is getting from this InputStream.
        * And don't know why, there should get a new InputStream from channelShell.
        **/
        InputStream in = channelShell.getInputStream();
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                shell.print(new String(tmp, 0, i));
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
            if (Cache.JUST_CLOSE_EXHIBIT_SHELL) {
                Cache.JUST_CLOSE_EXHIBIT_SHELL = false;
                break;
            }
        }
        promise.complete();
    }

    @Override
    protected <T> void resultWithin(AsyncResult<Void> asyncResult, Message<T> message) throws Exception {

    }
}
