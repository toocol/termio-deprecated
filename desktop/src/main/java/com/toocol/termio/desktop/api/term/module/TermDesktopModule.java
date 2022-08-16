package com.toocol.termio.desktop.api.term.module;

import com.toocol.termio.desktop.api.term.handlers.BlockingDesktopAcceptCommandHandler;
import com.toocol.termio.desktop.api.term.handlers.DesktopExecuteCommandHandler;
import com.toocol.termio.utilities.module.AbstractModule;
import com.toocol.termio.utilities.module.ModuleDeployment;
import com.toocol.termio.utilities.module.RegisterHandler;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:30
 */
@ModuleDeployment(weight = 10, worker = true, workerPoolName = "term-console-worker-pool")
@RegisterHandler(handlers = {
        BlockingDesktopAcceptCommandHandler.class,
        DesktopExecuteCommandHandler.class
})
public final class TermDesktopModule extends AbstractModule {

    @Override
    public void start() throws Exception {
        mountHandler(vertx, context);
    }

}
