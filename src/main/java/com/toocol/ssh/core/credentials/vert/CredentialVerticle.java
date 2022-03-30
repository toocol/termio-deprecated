package com.toocol.ssh.core.credentials.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.handler.IHandlerAssembler;
import com.toocol.ssh.common.utils.FileUtils;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.credentials.vo.SshCredential;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static com.toocol.ssh.core.file.FileVerticleAddress.ADDRESS_CHECK_FILE_EXIST;
import static com.toocol.ssh.core.file.FileVerticleAddress.ADDRESS_READ_FILE;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 15:03
 */
@PreloadDeployment(weight = 1)
public class CredentialVerticle extends AbstractVerticle implements IHandlerAssembler {

    private final Map<String, SshCredential> credentialsMap = new ConcurrentHashMap<>();

    @Override
    public void start() throws Exception {
        final WorkerExecutor executor = vertx.createSharedWorkerExecutor("credential-worker");
        String filePath = FileUtils.relativeToFixed("/starter/credentials.json");

        CountDownLatch latch = new CountDownLatch(1);
        vertx.eventBus().send(ADDRESS_CHECK_FILE_EXIST.address(), filePath, reply -> latch.countDown());

        vertx.eventBus().send(ADDRESS_READ_FILE.address(), filePath, reply -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                PrintUtil.printErr("latch await error.");
                System.exit(-1);
            }
            JsonArray sshCredentials = cast(reply.result().body());

            sshCredentials.forEach(o -> {
                JsonObject credentialJsonObj = cast(o);
                SshCredential sshCredential = new SshCredential(
                        credentialJsonObj.getString("name"),
                        credentialJsonObj.getString("ip"),
                        credentialJsonObj.getString("user"),
                        credentialJsonObj.getString("password"),
                        credentialJsonObj.getString("note")
                );
                credentialsMap.put(sshCredential.getIp(), sshCredential);
            });
        });
    }
}
