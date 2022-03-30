package com.toocol.ssh.core.file.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.handler.IHandlerAssembler;
import com.toocol.ssh.common.utils.FileUtils;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.file.handlers.ReadCredentialHandler;
import com.toocol.ssh.core.file.handlers.WriteCredentialHandler;
import io.vertx.core.AbstractVerticle;

import static com.toocol.ssh.core.file.FileVerticleAddress.ADDRESS_READ_CREDENTIAL;

/**
 * read the ssh credential from the local file system.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:27
 */
@PreloadDeployment(weight = 1)
@RegisterHandler(handlers = {
        ReadCredentialHandler.class,
        WriteCredentialHandler.class
})
public class FileVerticle extends AbstractVerticle implements IHandlerAssembler {

    @Override
    public void start() throws Exception {
        boolean success = FileUtils.checkAndCreateFile(FileUtils.relativeToFixed("/starter/credentials.json"));
        if (!success) {
            throw new RuntimeException("Create credential file failed.");
        }

        assemble(vertx, null);

        vertx.eventBus().send(ADDRESS_READ_CREDENTIAL.address(), null);
        /* read the stored ssh credential from file system, if success deploy the view verticle */
        PrintUtil.println("Success start the ssh credential reader verticle.");
    }
}
