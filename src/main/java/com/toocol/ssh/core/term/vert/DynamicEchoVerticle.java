package com.toocol.ssh.core.term.vert;

import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.annotation.VerticleDeployment;
import com.toocol.ssh.common.handler.IHandlerMounter;
import com.toocol.ssh.core.term.handlers.DynamicEchoHandler;
import io.vertx.core.AbstractVerticle;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 23:52
 * @version: 0.0.1
 */
@VerticleDeployment(worker = true, workerPoolSize = 1, workerPoolName = "term-dynamic-worker-pool")
@RegisterHandler(handlers = {
        DynamicEchoHandler.class
})
public class DynamicEchoVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        mountHandler(vertx, context);
    }

}
