package com.toocol.termio.console.vert;

import com.toocol.termio.console.handlers.BlockingAcceptCommandHandler;
import com.toocol.termio.console.handlers.BlockingMonitorTerminalHandler;
import com.toocol.termio.console.handlers.ExecuteCommandHandler;
import com.toocol.termio.utilities.functional.RegisterHandler;
import com.toocol.termio.utilities.functional.VerticleDeployment;
import com.toocol.termio.utilities.handler.IHandlerMounter;
import io.vertx.core.AbstractVerticle;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:30
 */
@VerticleDeployment(weight = 10, worker = true, workerPoolName = "term-console-worker-pool")
@RegisterHandler(handlers = {
        BlockingMonitorTerminalHandler.class,
        BlockingAcceptCommandHandler.class,
        ExecuteCommandHandler.class
})
public final class TermConsoleVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        mountHandler(vertx, context);
    }

}
