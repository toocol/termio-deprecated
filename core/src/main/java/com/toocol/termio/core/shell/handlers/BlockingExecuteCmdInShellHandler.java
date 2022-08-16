package com.toocol.termio.core.shell.handlers;

import com.toocol.termio.core.cache.ShellCache;
import com.toocol.termio.core.cache.StatusCache;
import com.toocol.termio.core.shell.core.CmdFeedbackHelper;
import com.toocol.termio.core.shell.core.Shell;
import com.toocol.termio.core.shell.core.ShellProtocol;
import com.toocol.termio.utilities.address.IAddress;
import com.toocol.termio.utilities.module.BlockingMessageHandler;
import com.toocol.termio.utilities.sync.SharedCountdownLatch;
import com.toocol.termio.utilities.utils.StrUtil;
import com.toocol.termio.core.shell.ShellAddress;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/10 22:45
 * @version: 0.0.1
 */
public final class BlockingExecuteCmdInShellHandler extends BlockingMessageHandler<String> {

    private final ShellCache shellCache = ShellCache.getInstance();

    public BlockingExecuteCmdInShellHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return ShellAddress.EXECUTE_SINGLE_COMMAND_IN_CERTAIN_SHELL;
    }

    @Override
    protected <T> void handleBlocking(Promise<String> promise, Message<T> message) throws Exception {
        JsonObject request = cast(message.body());
        Long sessionId = request.getLong("sessionId");
        String cmd = request.getString("cmd");
        String prefix = request.getString("prefix");

        SharedCountdownLatch.await(
                () -> {
                    StatusCache.JUST_CLOSE_EXHIBIT_SHELL = true;
                    StatusCache.EXHIBIT_WAITING_BEFORE_COMMAND_PREPARE = false;
                },
                this.getClass(),
                BlockingShellDisplayHandler.class
        );

        Shell shell = shellCache.getShell(sessionId);

        InputStream inputStream = shell.getInputStream(ShellProtocol.SSH);
        OutputStream outputStream = shell.getOutputStream(ShellProtocol.SSH);
        if (outputStream == null) {
            promise.complete("/");
            return;
        }
        outputStream.write((cmd + StrUtil.LF).getBytes(StandardCharsets.UTF_8));
        outputStream.flush();

        String feedback = new CmdFeedbackHelper(inputStream, cmd, shell, prefix).extractFeedback();

        StatusCache.ACCESS_EXHIBIT_SHELL_WITH_PROMPT = false;
        eventBus.send(ShellAddress.DISPLAY_SHELL.address(), sessionId);

        promise.complete(feedback);
    }

    @Override
    protected <T> void resultBlocking(AsyncResult<String> asyncResult, Message<T> message) throws Exception {
        if (asyncResult.succeeded()) {
            message.reply(asyncResult.result());
        } else {
            message.reply(null);
        }
    }
}
