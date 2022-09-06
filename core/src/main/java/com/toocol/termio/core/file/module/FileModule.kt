package com.toocol.termio.core.file.module;

import com.toocol.termio.core.file.handlers.BlockingCheckFileExistHandler;
import com.toocol.termio.core.file.handlers.BlockingChooseDirectoryHandler;
import com.toocol.termio.core.file.handlers.BlockingChooseFileHandler;
import com.toocol.termio.core.file.handlers.BlockingReadFileHandler;
import com.toocol.termio.utilities.module.AbstractModule;
import com.toocol.termio.utilities.module.ModuleDeployment;
import com.toocol.termio.utilities.module.RegisterHandler;

/**
 * read/write file through the local file system.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:27
 */
@ModuleDeployment(weight = 10, worker = true, workerPoolSize = 5, workerPoolName = "file-worker-pool")
@RegisterHandler(handlers = {
        BlockingCheckFileExistHandler.class,
        BlockingReadFileHandler.class,
        BlockingChooseFileHandler.class,
        BlockingChooseDirectoryHandler.class

})
public final class FileModule extends AbstractModule {

    @Override
    public void start() throws Exception {
        mountHandler(vertx, context);
    }

}
