package com.toocol.ssh.core.ssh.handlers;

import com.jcraft.jsch.ChannelShell;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.ssh.cache.CommandCache;
import com.toocol.ssh.core.ssh.cache.SessionCache;
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

import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_ACCEPT_COMMAND;
import static com.toocol.ssh.core.ssh.SshVerticleAddress.ACCEPT_SHELL_CMD;
import static com.toocol.ssh.core.ssh.constants.ShellCommands.CLEAR;
import static com.toocol.ssh.core.ssh.constants.ShellCommands.EXIT;

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
        ChannelShell channelShell = sessionCache.getChannelShell(sessionId);
        OutputStream outputStream = channelShell.getOutputStream();

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
                e.printStackTrace();
            }
        });

        while (true) {
            String cmd;
            try {
                Scanner scanner = new Scanner(System.in);
                cmd = scanner.nextLine();
            } catch (Exception e) {
                continue;
            }

            if (EXIT.equals(cmd)) {
                future.complete(sessionId);
                break;
            } else if (CLEAR.equals(cmd)) {
                PrintUtil.clear();
                PrintUtil.printTitle();
                cmd = "";
            } else if (isViVimCmd(cmd)) {
                System.out.println("Don't support vi/vim for now.");
                cmd = "";
            }

            CommandCache.CURRENT_COMMAND = cmd + "\r\n";
            cmd += ("\n");
            outputStream.write(cmd.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
    }

    @Override
    protected <T> void resultWithin(AsyncResult<Long> asyncResult, Message<T> message) throws Exception {
        long sessionId = asyncResult.result();
        sessionCache.stopChannel(sessionId);
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
