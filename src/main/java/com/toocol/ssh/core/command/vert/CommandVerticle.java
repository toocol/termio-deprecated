package com.toocol.ssh.core.command.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.handler.IHandlerAssembler;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.command.handlers.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;

/**
 * accept the outside(user)'s command;
 * execute the inside(shell) and outside(user)'s command.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 18:27
 */
@PreloadDeployment
@RegisterHandler(handlers = {
        AcceptAnyKeyHandler.class,
        AcceptOutsideCommandHandler.class,
        ClearScreenHandler.class,
        ExecuteExternalShellHandler.class,
        ExecuteOutsideCommandHandler.class
})
public class CommandVerticle extends AbstractVerticle implements IHandlerAssembler {

    @Override
    public void start() throws Exception {
        final WorkerExecutor executor = vertx.createSharedWorkerExecutor("command-worker");

        assemble(vertx, executor);

        PrintUtil.println("Success start the command acceptor verticle.");
    }

}
