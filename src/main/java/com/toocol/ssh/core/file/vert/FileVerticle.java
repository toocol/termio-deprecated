package com.toocol.ssh.core.file.vert;

import com.toocol.ssh.common.annotation.VerticleDeployment;
import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.handler.IHandlerMounter;
import com.toocol.ssh.core.file.handlers.CheckFileExistHandler;
import com.toocol.ssh.core.file.handlers.ChooseFileHandler;
import com.toocol.ssh.core.file.handlers.ReadFileHandler;
import io.vertx.core.AbstractVerticle;

/**
 * read/write file through the local file system.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:27
 */
@VerticleDeployment(weight = 10, worker = true, workerPoolSize = 2, workerPoolName = "file-worker-pool")
@RegisterHandler(handlers = {
        CheckFileExistHandler.class,
        ReadFileHandler.class,
        ChooseFileHandler.class
})
public final class FileVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        mountHandler(vertx, context);
    }

}
