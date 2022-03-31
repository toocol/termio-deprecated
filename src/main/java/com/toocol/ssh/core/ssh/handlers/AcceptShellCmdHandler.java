package com.toocol.ssh.core.ssh.handlers;

import com.jcraft.jsch.ChannelShell;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.router.IAddress;
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
import static com.toocol.ssh.core.ssh.constants.ShellCommands.*;

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

        final StringBuffer cmd = new StringBuffer();
        final StringBuffer lastCmd = new StringBuffer();
        /*
         *  block ctrl+c.
         *  after run this method, previous signal handler defined in TerminalSystem will be replaced by this one.
         */
        Signal.handle(new Signal("INT"), signal -> {
            try {
                if (TOP.equals(lastCmd.toString().split("\n")[0])) {
                    cmd.delete(0, cmd.length());
                    cmd.append("q:\n");
                    outputStream.write(cmd.toString().getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                    cmd.delete(0, cmd.length());
                } else {
                    channelShell.sendSignal("2");
                }
            } catch (Exception e) {
                // do nothing
                e.printStackTrace();
            }
        });

        boolean shitHappen = false;
        while (true) {
            if (!shitHappen) {
                lastCmd.delete(0, lastCmd.length());
                lastCmd.append(cmd);
            } else {
                shitHappen = false;
            }

            cmd.delete(0, cmd.length());
            try {
                Scanner scanner = new Scanner(System.in);
                cmd.append(scanner.nextLine());
            } catch (Exception e) {
                shitHappen = true;
                continue;
            }

            if (EXIT.equals(cmd.toString())) {
                future.complete(sessionId);
                break;
            } else if (CLEAR.equals(cmd.toString())) {
                PrintUtil.clear();
                PrintUtil.printTitle();
                cmd.delete(0, cmd.length());
            } else if (isViVimCmd(cmd.toString())) {
                System.out.print("Don't support vi/vim for now.");
                cmd.delete(0, cmd.length());
            }

            CommandCache.CURRENT_COMMAND = cmd + "\r\n";
            cmd.append("\n");
            outputStream.write(cmd.toString().getBytes(StandardCharsets.UTF_8));
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
