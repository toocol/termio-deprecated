package com.toocol.ssh.core.shell.handlers;

import com.jcraft.jsch.ChannelShell;
import com.toocol.ssh.core.cache.ShellCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.anis.Printer;
import com.toocol.ssh.utilities.handler.BlockingMessageHandler;
import com.toocol.ssh.utilities.log.Loggable;
import com.toocol.ssh.utilities.sync.SharedCountdownLatch;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.toocol.ssh.core.shell.ShellAddress.DISPLAY_SHELL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 15:44
 */
@SuppressWarnings("all")
public final class BlockingShellDisplayHandler extends BlockingMessageHandler<Long> implements Loggable {

    private final SshSessionCache sshSessionCache = SshSessionCache.getInstance();
    private final ShellCache shellCache = ShellCache.getInstance();

    private volatile boolean cmdHasFeedbackWhenJustExit = false;

    private volatile long firstIn = 0;

    public BlockingShellDisplayHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return DISPLAY_SHELL;
    }

    @Override
    protected <T> void handleBlocking(Promise<Long> promise, Message<T> message) throws Exception {
        long sessionId = cast(message.body());

        Shell shell = shellCache.getShell(sessionId);

        if (shell.hasWelcome() && StatusCache.SHOW_WELCOME) {
            shell.printWelcome();
            StatusCache.SHOW_WELCOME = false;
        }

        if (StatusCache.ACCESS_EXHIBIT_SHELL_WITH_PROMPT) {
            Printer.print(shell.getPrompt());
        } else {
            StatusCache.ACCESS_EXHIBIT_SHELL_WITH_PROMPT = true;
        }

        /*
         * All the remote feedback data is getting from this InputStream.
         * And don't know why, there should get a new InputStream from channelShell.
         **/
        InputStream in = shell.getInputStream();
        byte[] tmp = new byte[1024];

        while (true) {
            while (in.available() > 0) {
                if (StatusCache.EXHIBIT_WAITING_BEFORE_COMMAND_PREPARE) {
                    continue;
                }
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }

                boolean hasPrint = shell.print(new String(tmp, 0, i, StandardCharsets.UTF_8));
                if (hasPrint) {
                    int[] position = Term.getInstance().getCursorPosition();
                }
                if (hasPrint && StatusCache.JUST_CLOSE_EXHIBIT_SHELL) {
                    cmdHasFeedbackWhenJustExit = true;
                }
                SharedCountdownLatch.countdown(BlockingShellExecuteHandler.class, this.getClass());
            }

            if (StatusCache.HANGED_QUIT) {
                if (in.available() > 0) {
                    continue;
                }
                break;
            }
            if (shell.isClosed()) {
                if (in.available() > 0) {
                    continue;
                }
                break;
            }
            if (StatusCache.JUST_CLOSE_EXHIBIT_SHELL) {
                if (firstIn == 0) {
                    firstIn = System.currentTimeMillis();
                } else {
                    if (System.currentTimeMillis() - firstIn >= 2000) {
                        if (in.available() > 0) {
                            continue;
                        }
                        firstIn = 0;
                        break;
                    }
                }

                if (cmdHasFeedbackWhenJustExit) {
                    if (in.available() > 0) {
                        continue;
                    }
                    firstIn = 0;
                    break;
                }
            }
            /*
             * Reduce CPU utilization
             */
            Thread.sleep(1);
        }

        promise.complete(sessionId);
    }

    @Override
    protected <T> void resultBlocking(AsyncResult<Long> asyncResult, Message<T> message) throws Exception {
        if (StatusCache.JUST_CLOSE_EXHIBIT_SHELL) {
            StatusCache.JUST_CLOSE_EXHIBIT_SHELL = false;
            cmdHasFeedbackWhenJustExit = false;
            SharedCountdownLatch.countdown(BlockingExecuteCmdInShellHandler.class, this.getClass());
            return;
        }
        if (StatusCache.ACCEPT_SHELL_CMD_IS_RUNNING) {
            Long sessionId = asyncResult.result();
            ChannelShell channelShell = SshSessionCache.getInstance().getChannelShell(sessionId);
            if (channelShell != null && !channelShell.isClosed()) {
                eventBus.send(DISPLAY_SHELL.address(), sessionId);
            }
        }
    }
}
