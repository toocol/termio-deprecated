package com.toocol.ssh.core.file.handlers;

import com.toocol.ssh.common.handler.AbstractBlockingMessageHandler;
import com.toocol.ssh.common.address.IAddress;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import org.apache.commons.lang3.StringUtils;

import static com.toocol.ssh.core.file.FileAddress.ADDRESS_READ_FILE;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:35
 */
public final class BlockingReadFileHandler extends AbstractBlockingMessageHandler<String> {

    public BlockingReadFileHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return ADDRESS_READ_FILE;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<String> promise, Message<T> message) {
        String filePath = cast(message.body());
        Buffer resultBuffer = vertx.fileSystem().readFileBlocking(filePath);
        String fileData = resultBuffer.getString(0, resultBuffer.length());

        promise.complete(fileData);
    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<String> asyncResult, Message<T> message) {
        String result = asyncResult.result();
        message.reply(StringUtils.isEmpty(result) ? new JsonArray() : new JsonArray(result));
    }
}
