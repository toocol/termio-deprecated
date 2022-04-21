package com.toocol.ssh.core.shell.vert;

import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.annotation.VerticleDeployment;
import com.toocol.ssh.common.handler.IHandlerMounter;
import com.toocol.ssh.core.shell.handlers.*;
import io.vertx.core.AbstractVerticle;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:30
 */
@VerticleDeployment(worker = true, workerPoolSize = 5, workerPoolName = "shell-worker-pool")
@RegisterHandler(handlers = {
        BlockingEstablishSessionHandler.class,
        BlockingShellDisplayHandler.class,
        BlockingShellReceiveHandler.class,
        BlockingExecuteSingleCmdHandler.class,
        BlockingExecuteCmdInShellHandler.class,
        BlockingDfHandler.class,
        BlockingUfHandler.class
})
public final class ShellVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        mountHandler(vertx, context, true);
    }

}
