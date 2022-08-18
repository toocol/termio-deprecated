package com.toocol.termio.core.shell.handlers;

import com.jcraft.jsch.ChannelExec;
import com.toocol.termio.core.cache.ShellCache;
import com.toocol.termio.core.shell.core.CmdFeedbackHelper;
import com.toocol.termio.core.shell.core.ExecChannelProvider;
import com.toocol.termio.core.shell.core.Shell;
import com.toocol.termio.utilities.module.IAddress;
import com.toocol.termio.utilities.module.BlockingMessageHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.io.InputStream;

import static com.toocol.termio.core.shell.ShellAddress.EXECUTE_SINGLE_COMMAND;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/10 17:57
 * @version: 0.0.1
 */
public final class BlockingExecuteSingleCmdHandler extends BlockingMessageHandler<String> {

    private final ShellCache.Instance shellCache = ShellCache.Instance;
    private final ExecChannelProvider execChannelProvider = ExecChannelProvider.getInstance();

    public BlockingExecuteSingleCmdHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return EXECUTE_SINGLE_COMMAND;
    }

    @Override
    protected <T> void handleBlocking(Promise<String> promise, Message<T> message) throws Exception {
        JsonObject request = cast(message.body());
        Long sessionId = request.getLong("sessionId");
        String cmd = request.getString("cmd");
        String prefix = request.getString("prefix");

        ChannelExec channelExec = execChannelProvider.getChannelExec(sessionId);
        Shell shell = shellCache.getShell(sessionId);

        if (channelExec == null || shell == null) {
            promise.fail("ChannelExec or shell is null.");
            return;
        }
        InputStream inputStream = channelExec.getInputStream();

        channelExec.setCommand(cmd);
        channelExec.connect();

        String feedback = new CmdFeedbackHelper(inputStream, cmd, shell, prefix).extractFeedback();

        channelExec.disconnect();

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
