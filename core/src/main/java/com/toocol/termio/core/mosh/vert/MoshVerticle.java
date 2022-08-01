package com.toocol.termio.core.mosh.vert;

import com.toocol.termio.core.mosh.handlers.BlockingEstablishMoshSessionHandler;
import com.toocol.termio.utilities.functional.RegisterHandler;
import com.toocol.termio.utilities.functional.VerticleDeployment;
import com.toocol.termio.utilities.handler.IHandlerMounter;
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
