package com.toocol.termio.core.auth.handlers;

import com.toocol.termio.core.auth.core.SecurityCoder;
import com.toocol.termio.core.auth.core.SshCredential;
import com.toocol.termio.core.cache.CredentialCache;
import com.toocol.termio.utilities.address.IAddress;
import com.toocol.termio.utilities.handler.NonBlockingMessageHandler;
import com.toocol.termio.utilities.utils.FileUtil;
import com.toocol.termio.utilities.utils.MessageBox;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;

import static com.toocol.termio.core.auth.AuthAddress.ADD_CREDENTIAL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 16:18
 */
public final class AddCredentialHandler extends NonBlockingMessageHandler {

    private final CredentialCache credentialCache = CredentialCache.getInstance();

    public AddCredentialHandler(Vertx vertx, Context context) {
        super(vertx, context);
    }

    @Override
    public IAddress consume() {
        return ADD_CREDENTIAL;
    }

    @Override
    public <T> void handleInline(Message<T> message) {
        SshCredential credential = SshCredential.transFromJson(cast(message.body()));
        credentialCache.addCredential(credential);

        String filePath = FileUtil.relativeToFixed("./.credentials");
        String credentialsJson = credentialCache.getCredentialsJson();

        SecurityCoder coder = SecurityCoder.get();
        if (coder != null) {
            credentialsJson = coder.encode(credentialsJson);

            if (credentialsJson == null) {
                MessageBox.setExitMessage("Illegal program: the program seems to have been tampered. Please download the official version at https://github.com/Joezeo/termio" +
                        ", and try to delete unsafe .credentials at program's home folder.");
                System.exit(-1);
            }
        }

        vertx.fileSystem().writeFile(filePath, Buffer.buffer(credentialsJson), result -> {
        });

        message.reply(null);
    }

}
