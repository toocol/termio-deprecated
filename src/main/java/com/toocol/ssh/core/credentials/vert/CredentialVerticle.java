package com.toocol.ssh.core.credentials.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.handler.IHandlerMounter;
import com.toocol.ssh.common.utils.FileUtils;
import com.toocol.ssh.core.cache.Cache;
import com.toocol.ssh.core.credentials.handlers.AddCredentialHandler;
import com.toocol.ssh.core.credentials.vo.SshCredential;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import static com.toocol.ssh.core.file.FileVerticleAddress.ADDRESS_CHECK_FILE_EXIST;
import static com.toocol.ssh.core.file.FileVerticleAddress.ADDRESS_READ_FILE;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 15:03
 */
@PreloadDeployment(weight = 1)
@RegisterHandler(handlers = {
        AddCredentialHandler.class
})
public class CredentialVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        final WorkerExecutor executor = vertx.createSharedWorkerExecutor("credential-worker");
        String filePath = FileUtils.relativeToFixed("/starter/credentials.json");

        mountHandler(vertx, executor);

        executor.executeBlocking(future -> {
            vertx.eventBus().send(ADDRESS_CHECK_FILE_EXIST.address(), filePath, reply -> future.complete());
        }, result -> {
            vertx.eventBus().send(ADDRESS_READ_FILE.address(), filePath, reply -> {
                JsonArray sshCredentials = cast(reply.result().body());

                sshCredentials.forEach(o -> {
                    JsonObject credentialJsonObj = cast(o);
                    SshCredential sshCredential = SshCredential.transFromJson(credentialJsonObj);
                    Cache.addCredential(sshCredential);
                });
            });
        });

    }
}
