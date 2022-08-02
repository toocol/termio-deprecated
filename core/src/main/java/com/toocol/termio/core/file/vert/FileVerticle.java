package com.toocol.termio.core.file.vert;

import com.toocol.termio.core.file.handlers.BlockingCheckFileExistHandler;
import com.toocol.termio.core.file.handlers.BlockingChooseDirectoryHandler;
import com.toocol.termio.core.file.handlers.BlockingChooseFileHandler;
import com.toocol.termio.core.file.handlers.BlockingReadFileHandler;
import com.toocol.termio.utilities.functional.RegisterHandler;
import com.toocol.termio.utilities.functional.VerticleDeployment;
import com.toocol.termio.utilities.handler.IHandlerMounter;
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
