package com.toocol.ssh.core.shell.handlers;

import com.jcraft.jsch.ChannelShell;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.handler.AbstractBlockingMessageHandler;
import com.toocol.ssh.utilities.sync.SharedCountdownLatch;
import com.toocol.ssh.utilities.utils.StrUtil;
import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.shell.core.CmdFeedbackHelper;
import com.toocol.ssh.core.shell.core.Shell;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.toocol.ssh.core.shell.ShellAddress.EXECUTE_SINGLE_COMMAND_IN_CERTAIN_SHELL;
import static com.toocol.ssh.core.shell.ShellAddress.DISPLAY_SHELL;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/10 22:45
 * @version: 0.0.1
 */
public final class BlockingExecuteCmdInShellHandler extends AbstractBlockingMessageHandler<String> {

    private final SessionCache sessionCache = SessionCache.getInstance();

    public BlockingExecuteCmdInShellHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return EXECUTE_SINGLE_COMMAND_IN_CERTAIN_SHELL;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<String> promise, Message<T> message) throws Exception {
        JsonObject request = cast(message.body());
        Long sessionId = request.getLong("sessionId");
        String cmd = request.getString("cmd");

        SharedCountdownLatch.await(
                () -> {
                    StatusCache.JUST_CLOSE_EXHIBIT_SHELL = true;
                    StatusCache.EXHIBIT_WAITING_BEFORE_COMMAND_PREPARE = false;
                },
                this.getClass(),
                BlockingShellDisplayHandler.class
        );

        ChannelShell channelShell = sessionCache.getChannelShell(sessionId);
        Shell shell = sessionCache.getShell(sessionId);

        if (channelShell == null || shell == null) {
            promise.fail("ChannelExec or shell is null.");
            return;
        }

        InputStream inputStream = channelShell.getInputStream();
        shell.writeAndFlush((cmd + StrUtil.LF).getBytes(StandardCharsets.UTF_8));

        String feedback = new CmdFeedbackHelper(inputStream, cmd, shell).extractFeedback();

        StatusCache.ACCESS_EXHIBIT_SHELL_WITH_PROMPT = false;
        eventBus.send(DISPLAY_SHELL.address(), sessionId);

        promise.complete(feedback);
    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<String> asyncResult, Message<T> message) throws Exception {
        if (asyncResult.succeeded()) {
            message.reply(asyncResult.result());
        } else {
            message.reply(null);
        }
    }
}
