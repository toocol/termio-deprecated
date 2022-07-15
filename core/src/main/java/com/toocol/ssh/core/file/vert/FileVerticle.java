package com.toocol.ssh.core.file.vert;

import com.toocol.ssh.core.file.handlers.BlockingCheckFileExistHandler;
import com.toocol.ssh.core.file.handlers.BlockingChooseDirectoryHandler;
import com.toocol.ssh.core.file.handlers.BlockingChooseFileHandler;
import com.toocol.ssh.core.file.handlers.BlockingReadFileHandler;
import com.toocol.ssh.utilities.functional.RegisterHandler;
import com.toocol.ssh.utilities.functional.VerticleDeployment;
import com.toocol.ssh.utilities.handler.IHandlerMounter;
import io.vertx.core.AbstractVerticle;

/**
 * read/write file through the local file system.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:27
 */
@VerticleDeployment(weight = 10, worker = true, workerPoolSize = 5, workerPoolName = "file-worker-pool")
@RegisterHandler(handlers = {
        BlockingCheckFileExistHandler.class,
        BlockingReadFileHandler.class,
        BlockingChooseFileHandler.class,
        BlockingChooseDirectoryHandler.class

})
public final class FileVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        mountHandler(vertx, context);
    }

}
