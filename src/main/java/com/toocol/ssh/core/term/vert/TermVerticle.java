package com.toocol.ssh.core.term.vert;

import com.toocol.ssh.common.annotation.VerticleDeployment;
import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.handler.IHandlerMounter;
import com.toocol.ssh.core.term.handlers.AcceptCommandHandler;
import com.toocol.ssh.core.term.handlers.ExecuteCommandHandler;
import com.toocol.ssh.core.term.handlers.MonitorTerminalHandler;
import io.vertx.core.AbstractVerticle;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:30
 */
@VerticleDeployment
@RegisterHandler(handlers = {
        MonitorTerminalHandler.class,
        AcceptCommandHandler.class,
        ExecuteCommandHandler.class
})
public final class TermVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        mountHandler(vertx, context, true);
    }

}
