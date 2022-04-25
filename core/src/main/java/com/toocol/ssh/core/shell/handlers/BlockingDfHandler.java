package com.toocol.ssh.core.shell.handlers;

import com.jcraft.jsch.ChannelSftp;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.handler.AbstractBlockingMessageHandler;
import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.shell.core.SftpChannelProvider;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.term.core.Printer;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static com.toocol.ssh.core.file.FileAddress.CHOOSE_DIRECTORY;
import static com.toocol.ssh.core.shell.ShellAddress.START_DF_COMMAND;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/9 16:39
 * @version: 0.0.1
 */
public final class BlockingDfHandler extends AbstractBlockingMessageHandler<byte[]> {

    private final SftpChannelProvider sftpChannelProvider = SftpChannelProvider.getInstance();

    public static final int DF_TYPE_FILE = 1;
    public static final int DF_TYPE_BYTE = 2;

    public BlockingDfHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return START_DF_COMMAND;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<byte[]> promise, Message<T> message) throws Exception {
        JsonObject request = cast(message.body());
        Long sessionId = request.getLong("sessionId");
        String remotePath = request.getString("remotePath");
        int type = Optional.ofNullable(request.getInteger("type")).orElse(0);

        if (type != DF_TYPE_FILE && type != DF_TYPE_BYTE) {
            promise.complete();
            return;
        }

        ChannelSftp channelSftp = sftpChannelProvider.getChannelSftp(sessionId);
        if (channelSftp == null) {
            SessionCache.getInstance().getShell(sessionId).printErr("Create sftp channel failed.");
            promise.complete();
            return;
        }

        if (type == DF_TYPE_FILE) {
            CountDownLatch latch = new CountDownLatch(1);
            StringBuilder localPathBuilder = new StringBuilder();
            eventBus.request(CHOOSE_DIRECTORY.address(), null, result -> {
                if (result.result() == null) {
                    localPathBuilder.append("-1");
                } else {
                    localPathBuilder.append(Objects.requireNonNullElse(result.result().body(), "-1"));
                }
                latch.countDown();
            });
            latch.await();

            Shell shell = SessionCache.getInstance().getShell(sessionId);
            Printer.print(shell.getPrompt());

            String storagePath = localPathBuilder.toString();
            if ("-1".equals(storagePath)) {
                promise.fail("-1");
                promise.tryComplete();
                return;
            }

            if (remotePath.contains(",")) {
                for (String rpath : remotePath.split(",")) {
                    try {
                        channelSftp.get(rpath, localPathBuilder.toString());
                    } catch (Exception e) {
                        Printer.println("\ndf: no such file '" + rpath + "'.");
                        Printer.print(shell.getPrompt() + shell.getCurrentPrint());
                    }
                }
            } else {
                try {
                    channelSftp.get(remotePath, localPathBuilder.toString());
                } catch (Exception e) {
                    Printer.println("\ndf: no such file '" + remotePath + "'.");
                    Printer.print(shell.getPrompt() + shell.getCurrentPrint());
                }
            }


            promise.tryComplete();
        } else {
            try {
                InputStream inputStream = channelSftp.get(remotePath);
                byte[] bytes = IOUtils.buffer(inputStream).readAllBytes();
                promise.complete(bytes);
            } catch (Exception e) {
                promise.complete();
            }
        }

    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<byte[]> asyncResult, Message<T> message) throws Exception {
        byte[] result = asyncResult.result();
        if (result != null && result.length > 0) {
            message.reply(result);
        }
    }

}
