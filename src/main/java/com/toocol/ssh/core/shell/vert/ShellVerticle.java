package com.toocol.ssh.core.shell.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.handler.IHandlerMounter;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.shell.handlers.OpenShellHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 20:43
 */
@PreloadDeployment
@RegisterHandler(handlers = {
        OpenShellHandler.class
})
public class ShellVerticle extends AbstractVerticle implements IHandlerMounter {
    @Override
    public void start() throws Exception {
        final WorkerExecutor executor = vertx.createSharedWorkerExecutor("shell-worker", 10);

        mountHandler(vertx, executor, true);

        PrintUtil.println("Success start shell verticle.");
    }
}
