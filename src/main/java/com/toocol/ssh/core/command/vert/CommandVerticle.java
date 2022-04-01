package com.toocol.ssh.core.command.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.handler.IHandlerMounter;
import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.core.command.handlers.AcceptOutsideCommandHandler;
import com.toocol.ssh.core.command.handlers.ExecuteOutsideCommandHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;

/**
 * execute the inside(shell) and outside(user)'s command.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 18:27
 */
@PreloadDeployment
@RegisterHandler(handlers = {
        AcceptOutsideCommandHandler.class,
        ExecuteOutsideCommandHandler.class
})
public class CommandVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        final WorkerExecutor executor = vertx.createSharedWorkerExecutor("command-worker", 2);

        mountHandler(vertx, executor, true);

        Printer.printlnWithLogo("Success start the command acceptor verticle.");
    }

}
