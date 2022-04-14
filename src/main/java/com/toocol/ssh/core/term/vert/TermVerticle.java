package com.toocol.ssh.core.term.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.handler.IHandlerMounter;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.handlers.AcceptOutsideCommandHandler;
import com.toocol.ssh.core.term.handlers.ExecuteOutsideCommandHandler;
import com.toocol.ssh.core.term.handlers.MonitorTerminalHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:30
 */
@PreloadDeployment
@RegisterHandler(handlers = {
        MonitorTerminalHandler.class,
        AcceptOutsideCommandHandler.class,
        ExecuteOutsideCommandHandler.class
})
public class TermVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        WorkerExecutor executor = vertx.createSharedWorkerExecutor("ssh-terminal-worker", 3);

        mountHandler(vertx, executor, true);

        Printer.printlnWithLogo("Success start terminal verticle.");
    }

}
