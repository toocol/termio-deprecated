package com.toocol.ssh.core.term.vert;

import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.annotation.VerticleDeployment;
import com.toocol.ssh.common.handler.IHandlerMounter;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.core.term.handlers.BlockingAcceptCommandHandler;
import com.toocol.ssh.core.term.handlers.ExecuteCommandHandler;
import com.toocol.ssh.core.term.handlers.BlockingMonitorTerminalHandler;
import com.toocol.ssh.core.term.handlers.TerminalEchoHandler;
import io.vertx.core.AbstractVerticle;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:30
 */
@VerticleDeployment(worker = true, workerPoolSize = 5, workerPoolName = "term-worker-pool")
@RegisterHandler(handlers = {
        BlockingMonitorTerminalHandler.class,
        BlockingAcceptCommandHandler.class,
        ExecuteCommandHandler.class,
        TerminalEchoHandler.class
})
public final class TermVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        Term.set(new Term(getVertx().eventBus()));
        mountHandler(vertx, context, true);
    }

}
