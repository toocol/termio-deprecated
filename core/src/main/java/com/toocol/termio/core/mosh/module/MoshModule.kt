package com.toocol.termio.core.mosh.module;

import com.toocol.termio.core.mosh.handlers.BlockingEstablishMoshSessionHandler;
import com.toocol.termio.utilities.module.AbstractModule;
import com.toocol.termio.utilities.module.ModuleDeployment;
import com.toocol.termio.utilities.module.RegisterHandler;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 19:54
 */
@ModuleDeployment(worker = true, workerPoolName = "mosh-worker-pool")
@RegisterHandler(handlers = {
        BlockingEstablishMoshSessionHandler.class
})
public final class MoshModule extends AbstractModule {
    @Override
    public void start() throws Exception {
        mountHandler(vertx, context);
    }
}
