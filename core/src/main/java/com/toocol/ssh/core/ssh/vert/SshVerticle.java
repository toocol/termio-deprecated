package com.toocol.ssh.core.ssh.vert;

import com.toocol.ssh.core.ssh.handlers.BlockingActiveSshSessionHandler;
import com.toocol.ssh.core.ssh.handlers.BlockingEstablishSshSessionHandler;
import com.toocol.ssh.utilities.annotation.RegisterHandler;
import com.toocol.ssh.utilities.annotation.VerticleDeployment;
import com.toocol.ssh.utilities.handler.IHandlerMounter;
import io.vertx.core.AbstractVerticle;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/21 22:48
 * @version: 0.0.1
 */
@VerticleDeployment(worker = true, workerPoolSize = 5, workerPoolName = "ssh-worker-pool")
@RegisterHandler(handlers = {
        BlockingActiveSshSessionHandler.class,
        BlockingEstablishSshSessionHandler.class,
})
public final class SshVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        mountHandler(vertx, context);
    }

}
