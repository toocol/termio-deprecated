package com.toocol.termio.core.auth.vert;

import com.toocol.termio.core.auth.core.SecurityCoder;
import com.toocol.termio.core.auth.core.SshCredential;
import com.toocol.termio.core.auth.handlers.AddCredentialHandler;
import com.toocol.termio.core.auth.handlers.DeleteCredentialHandler;
import com.toocol.termio.core.cache.CredentialCache;
import com.toocol.termio.utilities.anis.Printer;
import com.toocol.termio.utilities.functional.RegisterHandler;
import com.toocol.termio.utilities.functional.VerticleDeployment;
import com.toocol.termio.utilities.handler.IHandlerMounter;
import com.toocol.termio.utilities.utils.FileUtil;
import com.toocol.termio.utilities.utils.MessageBox;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;

import static com.toocol.termio.core.file.FileAddress.CHECK_FILE_EXIST;
import static com.toocol.termio.core.file.FileAddress.READ_FILE;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 15:03
 */
@VerticleDeployment
@RegisterHandler(handlers = {
        AddCredentialHandler.class,
        DeleteCredentialHandler.class
})
public final class AuthVerticle extends AbstractVerticle implements IHandlerMounter {

    private final CredentialCache credentialCache = CredentialCache.getInstance();

    @Override
    public void start() throws Exception {
        String filePath = FileUtil.relativeToFixed("./.credentials");

        mountHandler(vertx, context);

        SecurityCoder coder = SecurityCoder.get();

        context.executeBlocking(promise -> {
            vertx.eventBus().request(CHECK_FILE_EXIST.address(), filePath, reply -> promise.complete());
        }, result -> {
            vertx.eventBus().request(READ_FILE.address(), filePath, reply -> {
                String sshCredentialsStr = cast(reply.result().body());
                if (coder != null) {
                    sshCredentialsStr = coder.decode(sshCredentialsStr);

                    if (sshCredentialsStr == null) {
                        MessageBox.setExitMessage("Illegal program: the program seems to have been tampered. Please download the official version at https://github.com/Joezeo/termio" +
                                ", and try to delete unsafe .credentials at program's home folder.");
                        System.exit(-1);
                    }
                }

                JsonArray sshCredentials = null;
                try {
                    sshCredentials = StringUtils.isEmpty(sshCredentialsStr) ? new JsonArray() : new JsonArray(sshCredentialsStr);
                } catch (Exception e) {
                    MessageBox.setExitMessage("Illegal program: the program seems to have been tampered. Please download the official version at https://github.com/Joezeo/termio" +
                            ", and try to delete unsafe .credentials at program's home folder.");
                    System.exit(-1);
                }

                sshCredentials.forEach(o -> {
                    JsonObject credentialJsonObj = cast(o);
                    SshCredential sshCredential = SshCredential.transFromJson(credentialJsonObj);
                    credentialCache.addCredential(sshCredential);
                });

                Printer.LOADING_ACCOMPLISH = true;
            });
        });
    }

    @Override
    public void stop() throws Exception {

    }
}
