package com.toocol.ssh.core.shell.handlers;

import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.utils.CmdUtil;
import com.toocol.ssh.common.utils.StrUtil;
import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.shell.commands.ShellCommand;
import com.toocol.ssh.core.shell.core.Shell;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.toocol.ssh.core.cmd.CmdAddress.ADDRESS_ACCEPT_COMMAND;
import static com.toocol.ssh.core.shell.ShellVerticleAddress.ACCEPT_SHELL_CMD;
import static com.toocol.ssh.core.shell.ShellVerticleAddress.EXECUTE_SINGLE_COMMAND_IN_CERTAIN_SHELL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 15:25
 */
public class AcceptShellCmdHandler extends AbstractMessageHandler<Long> {

    private final SessionCache sessionCache = SessionCache.getInstance();

    public AcceptShellCmdHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress consume() {
        return ACCEPT_SHELL_CMD;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<Long> promise, Message<T> message) throws Exception {
        StatusCache.ACCEPT_SHELL_CMD_IS_RUNNING = true;

        long sessionId = cast(message.body());
        Shell shell = sessionCache.getShell(sessionId);
        OutputStream outputStream = shell.getOutputStream();

        outputStream.write("export HISTCONTROL=ignoreboth\n".getBytes(StandardCharsets.UTF_8));
        outputStream.flush();

        while (true) {
            StringBuilder cmd = new StringBuilder(shell.readCmd());

            AtomicBoolean isBreak = new AtomicBoolean();
            AtomicBoolean isContinue = new AtomicBoolean();
            ShellCommand.cmdOf(cmd.toString()).ifPresent(shellCommand -> {
                try {
                    String finalCmd = shellCommand.processCmd(eventBus, promise, sessionId, isBreak, cmd.toString());
                    cmd.delete(0, cmd.length());
                    if (finalCmd != null) {
                        cmd.append(finalCmd);
                    } else {
                        isContinue.set(true);
                    }
                } catch (Exception e) {
                    // do noting
                }
            });

            if (isBreak.get()) {
                break;
            }
            if (isContinue.get()) {
                continue;
            }

            if (shell.getStatus().equals(Shell.Status.VIM_BEFORE)) {
                shell.setStatus(Shell.Status.VIM_UNDER);
            }

            if (shell.getStatus().equals(Shell.Status.NORMAL)) {
                shell.localLastCmd.set(cmd + StrUtil.CRLF);
            }

            String actualCmd = cmd.toString().trim() + StrUtil.LF;
            outputStream.write(actualCmd.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            CountDownLatch latch = new CountDownLatch(1);
            if (CmdUtil.isCdCmd(shell.getLastRemoteCmd()) || CmdUtil.isCdCmd(cmd.toString())) {
                JsonObject request = new JsonObject();
                request.put("sessionId", sessionId);
                request.put("cmd", " pwd");
                eventBus.request(EXECUTE_SINGLE_COMMAND_IN_CERTAIN_SHELL.address(), request, result -> {
                    shell.setFullPath(cast(result.result().body()));
                    latch.countDown();
                });
            } else {
                latch.countDown();
            }
            latch.await();
        }
    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<Long> asyncResult, Message<T> message) throws Exception {
        StatusCache.ACCEPT_SHELL_CMD_IS_RUNNING = false;
        StatusCache.STOP_LISTEN_TERMINAL_SIZE_CHANGE = true;

        if (asyncResult.succeeded()) {
            long sessionId = asyncResult.result();
            sessionCache.stop(sessionId);
        } else {
            // hang up the session
            StatusCache.HANGED_QUIT = true;
        }

        eventBus.send(ADDRESS_ACCEPT_COMMAND.address(), 3);
    }

}
