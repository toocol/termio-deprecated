package com.toocol.termio.core.shell.handlers;

import com.toocol.termio.core.cache.ShellCache;
import com.toocol.termio.core.cache.SshSessionCache;
import com.toocol.termio.core.cache.StatusCache;
import com.toocol.termio.core.cache.StatusConstants;
import com.toocol.termio.core.shell.commands.ShellCommand;
import com.toocol.termio.core.shell.core.Shell;
import com.toocol.termio.core.shell.core.ShellProtocol;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.core.term.core.TermStatus;
import com.toocol.termio.utilities.module.IAddress;
import com.toocol.termio.utilities.execeptions.RemoteDisconnectException;
import com.toocol.termio.utilities.module.BlockingMessageHandler;
import com.toocol.termio.utilities.sync.SharedCountdownLatch;
import com.toocol.termio.utilities.utils.MessageBox;
import com.toocol.termio.utilities.utils.StrUtil;
import com.toocol.termio.utilities.utils.Tuple2;
import com.toocol.termio.core.shell.ShellAddress;
import com.toocol.termio.core.term.TermAddress;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 15:25
 */
public final class BlockingShellExecuteHandler extends BlockingMessageHandler<Long> {

    private final ShellCache.Instance shellCache = ShellCache.Instance;

    public BlockingShellExecuteHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @NotNull
    @Override
    public IAddress consume() {
        return ShellAddress.RECEIVE_SHELL;
    }

    @Override
    protected <T> void handleBlocking(@NotNull Promise<Long> promise, @NotNull Message<T> message) throws Exception {
        StatusCache.ACCEPT_SHELL_CMD_IS_RUNNING = true;

        long sessionId = cast(message.body());
        Shell shell = shellCache.getShell(sessionId);
        if (shell == null) {
            promise.fail("Shell is null.");
            return;
        }

        try {
            if (shell.protocol.equals(ShellProtocol.SSH)) {
                shell.writeAndFlush("export HISTCONTROL=ignoreboth\n".getBytes(StandardCharsets.UTF_8));
            }

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
                        Tuple2<String, Long> result = shellCommand.processCmd(eventBus, shell, isBreak, shell.getRemoteCmd());
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

                if (shell.status.equals(Shell.Status.NORMAL)) {
                    if (shell.protocol.equals(ShellProtocol.SSH)) {
                        shell.setLocalLastCmd(cmd + StrUtil.CRLF);
                    } else if (shell.protocol.equals(ShellProtocol.MOSH)) {
                        shell.setLocalLastCmd(cmd + StrUtil.LF);
                    }
                }

                if (SshSessionCache.Instance.isDisconnect(sessionId)) {
                    throw new RemoteDisconnectException("Session disconnect.");
                }

                CountDownLatch latch = new CountDownLatch(1);
                if (StatusCache.EXECUTE_CD_CMD) {
                    StatusCache.EXECUTE_CD_CMD = false;
                    StatusCache.EXHIBIT_WAITING_BEFORE_COMMAND_PREPARE = true;
                    JsonObject request = new JsonObject();
                    request.put("sessionId", sessionId);
                    request.put("cmd", " pwd");
                    request.put("prefix", "/");
                    eventBus.request(ShellAddress.EXECUTE_SINGLE_COMMAND_IN_CERTAIN_SHELL.address(), request, result -> {
                        shell.fullPath.set(cast(result.result().body()));
                        latch.countDown();
                        info("Current full path: {}", shell.fullPath.get());
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
                }, 1000, this.getClass(), BlockingShellDisplayHandler.class);
                if (remoteDisconnect.get()) {
                    promise.tryComplete(sessionId);
                    break;
                }

                if (SshSessionCache.Instance.isDisconnect(sessionId)) {
                    // check the session status before await
                    throw new RemoteDisconnectException("Session disconnect.");
                }
                latch.await();
            }
        } catch (RemoteDisconnectException e) {
            MessageBox.setErrorMessage(e.getMessage());
            promise.tryComplete(sessionId);
        }

    }

    @Override
    protected <T> void resultBlocking(@NotNull AsyncResult<Long> asyncResult, @NotNull Message<T> message) throws Exception {
        StatusCache.ACCEPT_SHELL_CMD_IS_RUNNING = false;
        Term.status = TermStatus.TERMIO;

        long sessionId = asyncResult.result();
        if (StatusCache.HANGED_QUIT) {
            // hang up the session
            info("Hang up session, sessionId = {}", sessionId);
        } else if (StatusCache.SWITCH_SESSION) {
            info("Hang up this session waiting to switch, sessionId = {}", sessionId);
            StatusCache.SWITCH_SESSION = false;
            StatusCache.SWITCH_SESSION_WAIT_HANG_PREVIOUS = true;
            return;
        } else {
            shellCache.stop(sessionId);
            info("Destroy session, sessionId = {}", sessionId);
        }

        eventBus.send(TermAddress.ACCEPT_COMMAND.address(), StatusConstants.NORMAL_BACK);
    }

}
