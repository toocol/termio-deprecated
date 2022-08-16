package com.toocol.termio.core.mosh.module;

import com.toocol.termio.core.mosh.handlers.SocketCloseHandler;
import com.toocol.termio.core.mosh.handlers.SocketListenHandler;
import com.toocol.termio.utilities.module.AbstractModule;
import com.toocol.termio.utilities.module.ModuleDeployment;
import com.toocol.termio.utilities.module.RegisterHandler;

/**
 * use event loop thread poll to handler socket receive action.
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/30 19:29
 * @version: 0.0.1
 */
@ModuleDeployment
@RegisterHandler(handlers = {
        SocketListenHandler.class,
        SocketCloseHandler.class
})
public final class SocketModule extends AbstractModule {

    @Override
    public void start() throws Exception {
        mountHandler(vertx, context);
    }

}
