package com.toocol.ssh.core.view.vert;

import com.toocol.ssh.common.annotation.FinalDeployment;
import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.handler.IHandlerMounter;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.view.handlers.ShowLoadingHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;

import static com.toocol.ssh.core.view.TerminalViewAddress.ADDRESS_LOADING;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:47
 */
@FinalDeployment
@RegisterHandler(handlers = {
        ShowLoadingHandler.class
})
public class TerminalViewVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        final WorkerExecutor executor = vertx.createSharedWorkerExecutor("terminal-view-worker");

        mountHandler(vertx, executor);

        PrintUtil.println("Success start the ssh terminal view verticle.");
        vertx.eventBus().send(ADDRESS_LOADING.address(), "start");
    }
}
