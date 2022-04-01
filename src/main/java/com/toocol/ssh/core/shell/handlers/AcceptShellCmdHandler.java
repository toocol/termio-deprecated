package com.toocol.ssh.core.shell.handlers;

import com.jcraft.jsch.ChannelShell;
import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.core.cache.Cache;
import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.shell.commands.ShellCommand;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;
import org.apache.commons.lang3.StringUtils;
import sun.misc.Signal;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_ACCEPT_COMMAND;
import static com.toocol.ssh.core.shell.ShellVerticleAddress.ACCEPT_SHELL_CMD;

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
    public IAddress consume() {
        return ACCEPT_SHELL_CMD;
    }

    @Override
    protected <T> void handleWithin(Future<Long> future, Message<T> message) throws Exception {
        long sessionId = cast(message.body());
        ChannelShell channelShell = sessionCache.getChannelShell(sessionId);
        OutputStream outputStream = channelShell.getOutputStream();

        if (Cache.HANGED_ENTER) {
            outputStream.write("\n".getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            Cache.HANGED_ENTER = false;
        }

        /*
         *  block ctrl+c.
         *  after run this method, previous signal handler defined in TerminalSystem will be replaced by this one.
         */
        Signal.handle(new Signal("INT"), signal -> {
            try {
                outputStream.write(3);
                outputStream.flush();
            } catch (Exception e) {
                // do nothing
            }
        });

        while (true) {
            final StringBuilder cmd = new StringBuilder();
            try {
                Scanner scanner = new Scanner(System.in);
                cmd.append(scanner.nextLine());
            } catch (Exception e) {
                continue;
            }

            AtomicBoolean isBreak = new AtomicBoolean();
            ShellCommand.cmdOf(cmd.toString()).ifPresent(shellCommand -> {
                try {
                    shellCommand.processCmd(eventBus, future, sessionId, isBreak);
                    cmd.delete(0, cmd.length());
                } catch (Exception e) {
                    // do noting
                }
            });
            if (isBreak.get()) {
                break;
            }
            if (isViVimCmd(cmd.toString())) {
                Printer.println("Don't support vi/vim for now.");
                cmd.delete(0, cmd.length());
            }

            Cache.CURRENT_COMMAND = cmd + "\r\n";
            cmd.append("\n");
            outputStream.write(cmd.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
    }

    @Override
    protected <T> void resultWithin(AsyncResult<Long> asyncResult, Message<T> message) throws Exception {
        if (asyncResult.succeeded()) {
            long sessionId = asyncResult.result();
            sessionCache.stop(sessionId);
        } else {
            // hang up the session
            Cache.HANGED_QUIT = true;
        }
        eventBus.send(ADDRESS_ACCEPT_COMMAND.address(), true);
    }

    @SafeVarargs
    @Override
    public final <T> void inject(T... objs) {
        sessionCache = cast(objs[2]);
    }

    private boolean isViVimCmd(String cmd) {
        cmd = cmd.toLowerCase();
        return StringUtils.startsWith(cmd, "vi ") || StringUtils.startsWith(cmd, "vim ")
                || StringUtils.startsWith(cmd, "sudo vi ") || StringUtils.startsWith(cmd, "sudo vim ")
                || "vi".equals(cmd) || "vim".equals(cmd);
    }
}
