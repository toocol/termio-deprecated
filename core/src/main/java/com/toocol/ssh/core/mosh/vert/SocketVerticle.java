package com.toocol.ssh.core.mosh.vert;

import com.toocol.ssh.core.mosh.handlers.SocketCloseHandler;
import com.toocol.ssh.core.mosh.handlers.SocketListenHandler;
import com.toocol.ssh.utilities.functional.RegisterHandler;
import com.toocol.ssh.utilities.functional.VerticleDeployment;
import com.toocol.ssh.utilities.handler.IHandlerMounter;
import io.vertx.core.AbstractVerticle;

/**
 * use event loop thread poll to handler socket receive action.
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/30 19:29
 * @version: 0.0.1
 */
@VerticleDeployment
@RegisterHandler(handlers = {
        SocketListenHandler.class,
        SocketCloseHandler.class
})
public final class SocketVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        mountHandler(vertx, context);
    }

}
