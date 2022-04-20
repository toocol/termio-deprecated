package com.toocol.ssh.core.auth.vert;

import com.toocol.ssh.common.annotation.VerticleDeployment;
import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.handler.IHandlerMounter;
import com.toocol.ssh.common.utils.FileUtil;
import com.toocol.ssh.core.auth.handlers.AddCredentialHandler;
import com.toocol.ssh.core.auth.handlers.DeleteCredentialHandler;
import com.toocol.ssh.core.auth.vo.SshCredential;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.StatusCache;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import static com.toocol.ssh.core.file.FileAddress.ADDRESS_CHECK_FILE_EXIST;
import static com.toocol.ssh.core.file.FileAddress.ADDRESS_READ_FILE;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 15:03
 */
@VerticleDeployment(weight = 1, worker = true, workerPoolSize = 2, workerPoolName = "auth-worker-pool")
@RegisterHandler(handlers = {
        AddCredentialHandler.class,
        DeleteCredentialHandler.class
})
public final class AuthVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        String filePath = FileUtil.relativeToFixed("./credentials.json");

        mountHandler(vertx, context);

        context.executeBlocking(promise -> {
            vertx.eventBus().request(ADDRESS_CHECK_FILE_EXIST.address(), filePath, reply -> promise.complete());
        }, result -> {
            vertx.eventBus().request(ADDRESS_READ_FILE.address(), filePath, reply -> {
                JsonArray sshCredentials = cast(reply.result().body());

                sshCredentials.forEach(o -> {
                    JsonObject credentialJsonObj = cast(o);
                    SshCredential sshCredential = SshCredential.transFromJson(credentialJsonObj);
                    CredentialCache.addCredential(sshCredential);
                });

                StatusCache.LOADING_ACCOMPLISH = true;
            });
        });
    }
}
