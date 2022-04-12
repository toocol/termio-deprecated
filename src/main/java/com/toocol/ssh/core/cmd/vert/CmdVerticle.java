package com.toocol.ssh.core.cmd.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.handler.IHandlerMounter;
import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.core.cmd.handlers.AcceptOutsideCommandHandler;
import com.toocol.ssh.core.cmd.handlers.ExecuteOutsideCommandHandler;
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
public class CmdVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        final WorkerExecutor executor = vertx.createSharedWorkerExecutor("command-worker", 2);

        mountHandler(vertx, executor, true);

        Printer.printlnWithLogo("Success start the command verticle.");
    }

}
