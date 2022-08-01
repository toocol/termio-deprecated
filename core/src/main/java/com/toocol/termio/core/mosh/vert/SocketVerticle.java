package com.toocol.termio.core.mosh.vert;

import com.toocol.termio.core.mosh.handlers.SocketCloseHandler;
import com.toocol.termio.core.mosh.handlers.SocketListenHandler;
import com.toocol.termio.utilities.functional.RegisterHandler;
import com.toocol.termio.utilities.functional.VerticleDeployment;
import com.toocol.termio.utilities.handler.IHandlerMounter;
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
