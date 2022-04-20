package com.toocol.ssh.core.shell.handlers;

import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.execeptions.RemoteDisconnectException;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.sync.SharedCountdownLatch;
import com.toocol.ssh.common.utils.StrUtil;
import com.toocol.ssh.common.utils.Tuple2;
import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.shell.commands.ShellCommand;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.term.handlers.AcceptCommandHandler;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.toocol.ssh.core.shell.ShellAddress.RECEIVE_SHELL;
import static com.toocol.ssh.core.shell.ShellAddress.EXECUTE_SINGLE_COMMAND_IN_CERTAIN_SHELL;
import static com.toocol.ssh.core.term.TermAddress.ADDRESS_ACCEPT_COMMAND;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 15:25
 */
public final class ShellReceiveHandler extends AbstractMessageHandler<Long> {

    private final SessionCache sessionCache = SessionCache.getInstance();

    public ShellReceiveHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return RECEIVE_SHELL;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<Long> promise, Message<T> message) throws Exception {
        StatusCache.ACCEPT_SHELL_CMD_IS_RUNNING = true;

        long sessionId = cast(message.body());
        Shell shell = sessionCache.getShell(sessionId);

        shell.writeAndFlush("export HISTCONTROL=ignoreboth\n".getBytes(StandardCharsets.UTF_8));

        try {
            while (true) {
                String cmdRead = shell.readCmd();
                if (cmdRead == null) {
                    continue;
                }
                StringBuilder cmd = new StringBuilder(cmdRead);

                AtomicBoolean isBreak = new AtomicBoolean();
                AtomicBoolean isContinue = new AtomicBoolean();
                AtomicReference<Long> completeSessionId = new AtomicReference<>();
                ShellCommand.cmdOf(cmd.toString()).ifPresent(shellCommand -> {
                    try {
                        if (shell.getRemoteCmd().length() != 0) {
                            return;
                        }
                        String finalCmd;
                        Tuple2<String, Long> result = shellCommand.processCmd(eventBus, shell, isBreak, cmd.toString());
                        finalCmd = result._1();
                        completeSessionId.set(result._2());
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
                ShellCommand.cmdOf(shell.getRemoteCmd()).ifPresent(shellCommand -> {
                    try {
                        String finalCmd;
                        Tuple2<String, Long> result = shellCommand.processCmd(eventBus, shell, isBreak, cmd.toString());
                        finalCmd = result._1();
                        completeSessionId.set(result._2());
                        if (finalCmd != null) {
                            cmd.append(finalCmd);
                        } else {
                            isContinue.set(true);
                        }
                    } catch (Exception e) {
                        // do noting
                    }
                });
                shell.clearRemoteCmd();

                if (isBreak.get()) {
                    if (cmd.length() != 0) {
                        shell.writeAndFlush(cmd.append("\t").toString().getBytes(StandardCharsets.UTF_8));
                    }
                    promise.complete(completeSessionId.get());
                    break;
                }
                if (isContinue.get()) {
                    continue;
                }

                if (shell.getStatus().equals(Shell.Status.NORMAL)) {
                    shell.setLocalLastCmd(cmd + StrUtil.CRLF);
                }

                if (SessionCache.getInstance().isDisconnect(sessionId)) {
                    throw new RemoteDisconnectException("Session disconnect.");
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

                AtomicBoolean remoteDisconnect = new AtomicBoolean();
                SharedCountdownLatch.await(() -> {
                    try {
                        shell.writeAndFlush(actualCmd.getBytes(StandardCharsets.UTF_8));
                    } catch (RemoteDisconnectException e) {
                        // do nothing
                        remoteDisconnect.set(true);
                    }
                }, 1000, this.getClass(), ShellDisplayHandler.class);
                if (remoteDisconnect.get()) {
                    promise.tryComplete(sessionId);
                    break;
                }

                if (SessionCache.getInstance().isDisconnect(sessionId)) {
                    // check the session status before await
                    throw new RemoteDisconnectException("Session disconnect.");
                }
                latch.await();
            }
        } catch (RemoteDisconnectException e) {
            promise.tryComplete(sessionId);
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

        eventBus.send(ADDRESS_ACCEPT_COMMAND.address(), AcceptCommandHandler.NORMAL_BACK);
    }

}
