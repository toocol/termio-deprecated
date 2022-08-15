package com.toocol.termio.core.term.vert;

import com.toocol.termio.core.term.handlers.desktop.BlockingDesktopAcceptCommandHandler;
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
        BlockingDesktopAcceptCommandHandler.class
})
public final class TermDesktopVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        mountHandler(vertx, context);
    }

}
