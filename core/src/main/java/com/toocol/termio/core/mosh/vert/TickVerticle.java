package com.toocol.termio.core.mosh.vert;

import com.toocol.termio.core.mosh.handlers.BlockingTickHandler;
import com.toocol.termio.utilities.functional.RegisterHandler;
import com.toocol.termio.utilities.functional.VerticleDeployment;
import com.toocol.termio.utilities.handler.IHandlerMounter;
import io.vertx.core.AbstractVerticle;

/**
 * use event loop thread poll to handler socket receive action.
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/30 19:29
 * @version: 0.0.1
 */
@VerticleDeployment(worker = true, workerPoolName = "mosh-tick-worker-pool")
@RegisterHandler(handlers = {
        BlockingTickHandler.class
})
public final class TickVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        mountHandler(vertx, context);
    }

}
