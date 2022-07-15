package com.toocol.ssh.core.shell.vert;

import com.toocol.ssh.core.shell.handlers.*;
import com.toocol.ssh.utilities.annotation.RegisterHandler;
import com.toocol.ssh.utilities.annotation.VerticleDeployment;
import com.toocol.ssh.utilities.handler.IHandlerMounter;
import io.vertx.core.AbstractVerticle;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/21 22:48
 * @version: 0.0.1
 */
@VerticleDeployment(worker = true, workerPoolName = "shell-worker-pool")
@RegisterHandler(handlers = {
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
        mountHandler(vertx, context);
    }

}
