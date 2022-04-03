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
import jline.ConsoleReader;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_ACCEPT_COMMAND;
import static com.toocol.ssh.core.shell.ShellVerticleAddress.ACCEPT_SHELL_CMD;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 15:25
 */
public class AcceptShellCmdHandler extends AbstractMessageHandler<Long> {

    final StringBuffer cmd = new StringBuffer();

    private SessionCache sessionCache;
    private ConsoleReader reader;

    public AcceptShellCmdHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
        try {
//            reader = new ConsoleReader(System.in, new PrintWriter(new OutputStreamWriter(System.out, System.getProperty("jline.WindowsTerminal.output.encoding", System.getProperty("file.encoding")))));
            reader = new ConsoleReader(System.in, null);
        } catch (IOException e) {
            Printer.println("Register console reader failed.");
            System.exit(-1);
        }
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

        while (true) {
            cmd.delete(0, cmd.length());
            while (true) {
                char inChar = (char) reader.readVirtualKey();
                if (inChar == '\t') {
                    Cache.CURRENT_COMMAND = cmd.append('\t').toString();
                    cmd.append(inChar);
                    outputStream.write(cmd.append('\t').toString().getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                    cmd.delete(0, cmd.length());
                } else if(inChar == '\b') {
                    if (cmd.toString().trim().length() == 0) {
                        continue;
                    }
                    Printer.print("\b");
                    Printer.print(" ");
                    Printer.print("\b");
                    cmd.deleteCharAt(cmd.length() - 1);
                } else if (inChar == '\r' || inChar == '\n') {
                    Printer.print("\r\n");
                    break;
                } else {
                    Printer.print(String.valueOf(inChar));
                    cmd.append(inChar);
                }
            }

            AtomicBoolean isBreak = new AtomicBoolean();
            ShellCommand.cmdOf(cmd.toString()).ifPresent(shellCommand -> {
                try {
                    String finalCmd = shellCommand.processCmd(eventBus, future, sessionId, isBreak);
                    cmd.delete(0, cmd.length());
                    cmd.append(finalCmd);
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
            String actualCmd = cmd.toString().trim() + "\n";
            outputStream.write(actualCmd.getBytes(StandardCharsets.UTF_8));
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
        eventBus.send(ADDRESS_ACCEPT_COMMAND.address(), 3);
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
