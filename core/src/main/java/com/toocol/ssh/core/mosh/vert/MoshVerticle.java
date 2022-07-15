package com.toocol.ssh.core.mosh.vert;

import com.toocol.ssh.core.mosh.handlers.BlockingEstablishMoshSessionHandler;
import com.toocol.ssh.utilities.functional.RegisterHandler;
import com.toocol.ssh.utilities.functional.VerticleDeployment;
import com.toocol.ssh.utilities.handler.IHandlerMounter;
import io.vertx.core.AbstractVerticle;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 19:54
 */
@VerticleDeployment(worker = true, workerPoolName = "mosh-worker-pool")
@RegisterHandler(handlers = {
        BlockingEstablishMoshSessionHandler.class
})
public final class MoshVerticle extends AbstractVerticle implements IHandlerMounter {
    @Override
    public void start() throws Exception {
        mountHandler(vertx, context);
    }
}
