package com.toocol.ssh.core.auth.handlers;

import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.utils.FileUtil;
import com.toocol.ssh.core.cache.CredentialCache;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.auth.AuthAddress.DELETE_CREDENTIAL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 16:18
 */
public final class DeleteCredentialHandler extends AbstractMessageHandler {

    public DeleteCredentialHandler(Vertx vertx, Context context) {
        super(vertx, context);
    }

    @Override
    public IAddress consume() {
        return DELETE_CREDENTIAL;
    }

    @Override
    public <T> void handle(Message<T> message) {
        int index = cast(message.body());
        CredentialCache.deleteCredential(index);

        String filePath = FileUtil.relativeToFixed("./credentials.json");
        vertx.fileSystem().writeFile(filePath, Buffer.buffer(CredentialCache.getCredentialsJson()), result -> {
        });
        message.reply(null);
    }

}
