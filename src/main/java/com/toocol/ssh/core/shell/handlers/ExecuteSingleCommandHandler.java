package com.toocol.ssh.core.shell.handlers;

import com.jcraft.jsch.ChannelExec;
import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.shell.core.CmdFeedbackHelper;
import com.toocol.ssh.core.shell.core.ExecChannelProvider;
import com.toocol.ssh.core.shell.core.Shell;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.io.InputStream;

import static com.toocol.ssh.core.shell.ShellAddress.EXECUTE_SINGLE_COMMAND;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/10 17:57
 * @version: 0.0.1
 */
public final class ExecuteSingleCommandHandler extends AbstractMessageHandler<String> {

    private final SessionCache sessionCache = SessionCache.getInstance();
    private final ExecChannelProvider execChannelProvider = ExecChannelProvider.getInstance();

    public ExecuteSingleCommandHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return EXECUTE_SINGLE_COMMAND;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<String> promise, Message<T> message) throws Exception {
        JsonObject request = cast(message.body());
        Long sessionId = request.getLong("sessionId");
        String cmd = request.getString("cmd");

        ChannelExec channelExec = execChannelProvider.getChannelExec(sessionId);
        Shell shell = sessionCache.getShell(sessionId);

        if (channelExec == null || shell == null) {
            promise.fail("ChannelExec or shell is null.");
            return;
        }
        InputStream inputStream = channelExec.getInputStream();

        channelExec.setCommand(cmd);
        channelExec.connect();

        String feedback = new CmdFeedbackHelper(inputStream, cmd, shell).extractFeedback();

        channelExec.disconnect();

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
