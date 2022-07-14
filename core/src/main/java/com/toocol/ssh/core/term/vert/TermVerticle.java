package com.toocol.ssh.core.term.vert;

import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.core.term.handlers.BlockingAcceptCommandHandler;
import com.toocol.ssh.core.term.handlers.BlockingMonitorTerminalHandler;
import com.toocol.ssh.core.term.handlers.ExecuteCommandHandler;
import com.toocol.ssh.utilities.annotation.RegisterHandler;
import com.toocol.ssh.utilities.annotation.VerticleDeployment;
import com.toocol.ssh.utilities.handler.IHandlerMounter;
import io.vertx.core.AbstractVerticle;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:30
 */
@VerticleDeployment(weight = 10, worker = true, workerPoolSize = 3, workerPoolName = "term-worker-pool")
@RegisterHandler(handlers = {
        BlockingMonitorTerminalHandler.class,
        BlockingAcceptCommandHandler.class,
        ExecuteCommandHandler.class
})
public final class TermVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        Term.setEventBus(vertx.eventBus());
        mountHandler(vertx, context, true);
    }

}
