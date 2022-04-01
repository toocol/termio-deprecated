package com.toocol.ssh.core.file.handlers;

import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.address.IAddress;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import org.apache.commons.lang3.StringUtils;

import static com.toocol.ssh.core.file.FileVerticleAddress.ADDRESS_READ_FILE;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:35
 */
public class ReadFileHandler extends AbstractMessageHandler<String> {

    public ReadFileHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress consume() {
        return ADDRESS_READ_FILE;
    }

    @Override
    protected <T> void handleWithin(Future<String> future, Message<T> message) {
        String filePath = cast(message.body());
        Buffer resultBuffer = vertx.fileSystem().readFileBlocking(filePath);
        String fileData = resultBuffer.getString(0, resultBuffer.length());

        future.complete(fileData);
    }

    @Override
    protected <T> void resultWithin(AsyncResult<String> asyncResult, Message<T> message) {
        String result = asyncResult.result();
        message.reply(StringUtils.isEmpty(result) ? new JsonArray() : new JsonArray(result));
    }
}
