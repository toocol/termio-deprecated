package com.toocol.termio.core.mosh.module;

import com.toocol.termio.core.mosh.handlers.BlockingTickHandler;
import com.toocol.termio.utilities.module.AbstractModule;
import com.toocol.termio.utilities.module.ModuleDeployment;
import com.toocol.termio.utilities.module.RegisterHandler;

/**
 * use event loop thread poll to handler socket receive action.
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/30 19:29
 * @version: 0.0.1
 */
@ModuleDeployment(worker = true, workerPoolName = "mosh-tick-worker-pool")
@RegisterHandler(handlers = {
        BlockingTickHandler.class
})
public final class TickModule extends AbstractModule {

    @Override
    public void start() throws Exception {
        mountHandler(vertx, context);
    }

}
