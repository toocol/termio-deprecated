package com.toocol.ssh.core.auth.handlers;

import com.toocol.ssh.core.auth.core.SecurityCoder;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.handler.AbstractMessageHandler;
import com.toocol.ssh.utilities.utils.FileUtil;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import org.apache.commons.lang3.StringUtils;

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
        String host = CredentialCache.deleteCredential(index);
        if (StringUtils.isNotEmpty(host)) {
            SshSessionCache.getInstance().stop(host);
        }

        String filePath = FileUtil.relativeToFixed("./credentials.tsh");
        String credentialsJson = CredentialCache.getCredentialsJson();

        SecurityCoder coder = SecurityCoder.get();
        if (coder != null) {
            credentialsJson = coder.encode(credentialsJson);

            if (credentialsJson == null) {
                Printer.clear();
                Printer.printErr("Illegal program: the program seems to have been tampered. Please download the official version at https://github.com/Joezeo/termio" +
                        ", and try to delete unsafe credentials.tsh at program's home folder.");
                System.exit(-1);
            }
        }

        vertx.fileSystem().writeFile(filePath, Buffer.buffer(credentialsJson), result -> {
        });

        message.reply(null);
    }

}
