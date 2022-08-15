package com.toocol.termio.core.shell.handlers;

import com.jcraft.jsch.ChannelSftp;
import com.toocol.termio.core.cache.ShellCache;
import com.toocol.termio.core.shell.core.SftpChannelProvider;
import com.toocol.termio.core.shell.core.Shell;
import com.toocol.termio.utilities.address.IAddress;
import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.handler.BlockingMessageHandler;
import com.toocol.termio.utilities.utils.FileNameUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.io.FileInputStream;
import java.util.Objects;

import static com.toocol.termio.core.file.FileAddress.CHOOSE_FILE;
import static com.toocol.termio.core.shell.ShellAddress.START_UF_COMMAND;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/9 16:38
 * @version: 0.0.1
 */
public final class BlockingUfHandler extends BlockingMessageHandler<Void> {

    private final SftpChannelProvider sftpChannelProvider = SftpChannelProvider.getInstance();
    private final ShellCache shellCache = ShellCache.getInstance();

    public BlockingUfHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return START_UF_COMMAND;
    }

    @Override
    protected <T> void handleBlocking(Promise<Void> promise, Message<T> message) throws Exception {
        JsonObject request = cast(message.body());
        Long sessionId = request.getLong("sessionId");
        String remotePath = request.getString("remotePath");

        ChannelSftp channelSftp = sftpChannelProvider.getChannelSftp(sessionId);
        if (channelSftp == null) {
            shellCache.getShell(sessionId).printErr("Create sftp channel failed.");
            promise.complete();
            return;
        }

        StringBuilder localPathBuilder = new StringBuilder();
        eventBus.request(CHOOSE_FILE.address(), null, result -> {
            localPathBuilder.append(Objects.requireNonNullElse(result.result().body(), "-1"));

            Shell shell = shellCache.getShell(sessionId);
            Printer.print(shell.getPrompt());

            String fileNames = localPathBuilder.toString();
            if ("-1".equals(fileNames)) {
                promise.tryFail("-1");
                return;
            }

            try {
                channelSftp.cd(remotePath);
                for (String fileName : fileNames.split(",")) {
                    channelSftp.put(new FileInputStream(fileName), FileNameUtil.getName(fileName));
                }
            } catch (Exception e) {
                // do nothing
            }
        });

        promise.tryComplete();
    }

    @Override
    protected <T> void resultBlocking(AsyncResult<Void> asyncResult, Message<T> message) throws Exception {

    }
}
