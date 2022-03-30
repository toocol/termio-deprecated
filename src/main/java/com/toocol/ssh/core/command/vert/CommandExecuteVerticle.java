package com.toocol.ssh.core.command.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.handler.IHandlerAssembler;
import com.toocol.ssh.common.utils.PrintUtil;
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
        ExecuteOutsideCommandHandler.class
})
public class CommandExecuteVerticle extends AbstractVerticle implements IHandlerAssembler {

    @Override
    public void start() throws Exception {
        final WorkerExecutor executor = vertx.createSharedWorkerExecutor("command-execute-worker");

        assemble(vertx, executor);

        PrintUtil.println("Success start the command acceptor verticle.");
    }

}
