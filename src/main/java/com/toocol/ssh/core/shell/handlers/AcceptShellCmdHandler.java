package com.toocol.ssh.core.shell.handlers;

import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
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

import static com.toocol.ssh.core.shell.ShellAddress.ACCEPT_SHELL_CMD;
import static com.toocol.ssh.core.shell.ShellAddress.EXECUTE_SINGLE_COMMAND_IN_CERTAIN_SHELL;
import static com.toocol.ssh.core.term.TermAddress.ADDRESS_ACCEPT_COMMAND;

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
            String cmdRead = shell.readCmd();
            if (cmdRead == null) {
                continue;
            }
            StringBuilder cmd = new StringBuilder(cmdRead);

            AtomicBoolean isBreak = new AtomicBoolean();
            AtomicBoolean isContinue = new AtomicBoolean();
            ShellCommand.cmdOf(cmd.toString()).ifPresent(shellCommand -> {
                try {
                    String finalCmd = shellCommand.processCmd(eventBus, promise, shell, isBreak, cmd.toString());
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
            ShellCommand.cmdOf(shell.remoteCmd.get()).ifPresent(shellCommand -> {
                try {
                    String finalCmd = shellCommand.processCmd(eventBus, promise, shell, isBreak, shell.remoteCmd.toString());
                    if (finalCmd != null) {
                        cmd.append(finalCmd);
                    } else {
                        isContinue.set(true);
                    }
                } catch (Exception e) {
                    // do noting
                }
            });
            shell.remoteCmd.set(StrUtil.EMPTY);

            if (isBreak.get()) {
                if (cmd.length() != 0) {
                    outputStream.write(cmd.append("\t").toString().getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                }
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

            CountDownLatch latch = new CountDownLatch(1);
            if (StatusCache.EXECUTE_CD_CMD) {
                StatusCache.EXECUTE_CD_CMD = false;
                StatusCache.EXHIBIT_WAITING_BEFORE_COMMAND_PREPARE = true;
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

            String actualCmd = cmd.toString().trim() + StrUtil.LF;
            outputStream.write(actualCmd.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            latch.await();
        }
    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<Long> asyncResult, Message<T> message) throws Exception {
        StatusCache.ACCEPT_SHELL_CMD_IS_RUNNING = false;
        StatusCache.STOP_LISTEN_TERMINAL_SIZE_CHANGE = true;

        long sessionId = asyncResult.result();
        if (sessionId == -1) {
            // hang up the session
            StatusCache.HANGED_QUIT = true;
        } else {
            sessionCache.stop(sessionId);
        }

        eventBus.send(ADDRESS_ACCEPT_COMMAND.address(), 1);
    }

}
