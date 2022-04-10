package com.toocol.ssh.core.shell.handlers;

import com.jcraft.jsch.ChannelSftp;
import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.utils.FileNameUtil;
import com.toocol.ssh.common.utils.FileUtil;
import com.toocol.ssh.common.utils.StrUtil;
import com.toocol.ssh.core.shell.core.SftpChannelProvider;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import static com.toocol.ssh.core.file.FileVerticleAddress.CHOOSE_FILE;
import static com.toocol.ssh.core.shell.ShellVerticleAddress.EXECUTE_SINGLE_COMMAND_IN_CERTAIN_SHELL;
import static com.toocol.ssh.core.shell.ShellVerticleAddress.START_UF_COMMAND;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/9 16:38
 * @version: 0.0.1
 */
public class UfHandler extends AbstractMessageHandler<Void> {

    private final SftpChannelProvider sftpChannelProvider = SftpChannelProvider.getInstance();

    public UfHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress consume() {
        return START_UF_COMMAND;
    }

    @Override
    protected <T> void handleWithin(Promise<Void> promise, Message<T> message) throws Exception {
        Long sessionId = cast(message.body());

        CountDownLatch latch = new CountDownLatch(2);
        JsonObject execRequest = new JsonObject();
        StringBuilder remotePathBuilder = new StringBuilder();
        execRequest.put("sessionId", sessionId);
        execRequest.put("cmd", "pwd");
        eventBus.request(EXECUTE_SINGLE_COMMAND_IN_CERTAIN_SHELL.address(), execRequest, result -> {
            remotePathBuilder.append(Objects.requireNonNullElse(result.result().body(), "-1"));
            latch.countDown();
        });


        StringBuilder localPathBuilder = new StringBuilder();
        eventBus.request(CHOOSE_FILE.address(), execRequest, result -> {
            localPathBuilder.append(Objects.requireNonNullElse(result.result().body(), "-1"));
            latch.countDown();
        });

        if ("-1".equals(remotePathBuilder.toString())) {
            promise.fail("-1");
        }
        if ("-1".equals(localPathBuilder.toString())) {
            promise.fail("-1");
        }

        ChannelSftp channelSftp = sftpChannelProvider.getChannelSftp(sessionId);
        latch.await();
        channelSftp.cd(remotePathBuilder.toString());
        channelSftp.put(new FileInputStream(localPathBuilder.toString()), FileNameUtil.getName(localPathBuilder.toString()));
    }

    @Override
    protected <T> void resultWithin(AsyncResult<Void> asyncResult, Message<T> message) throws Exception {

    }
}
