package com.toocol.ssh.core.ssh.vert;

import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.annotation.VerticleDeployment;
import com.toocol.ssh.common.handler.IHandlerMounter;
import com.toocol.ssh.core.ssh.handlers.BlockingEstablishSessionHandler;
import io.vertx.core.AbstractVerticle;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/21 22:48
 * @version: 0.0.1
 */
@VerticleDeployment(worker = true, workerPoolSize = 1, workerPoolName = "ssh-worker-pool")
@RegisterHandler(handlers = {
        BlockingEstablishSessionHandler.class,
})
public class SshVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        mountHandler(vertx, context, true);
    }

}
