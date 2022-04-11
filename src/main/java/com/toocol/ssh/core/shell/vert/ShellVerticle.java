package com.toocol.ssh.core.shell.vert;

import com.toocol.ssh.common.annotation.FinalDeployment;
import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.handler.IHandlerMounter;
import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.core.shell.handlers.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:30
 */
@FinalDeployment
@RegisterHandler(handlers = {
        EstablishSessionShellChannelHandler.class,
        ExhibitShellHandler.class,
        AcceptShellCmdHandler.class,
        ExecuteSingleCommandHandler.class,
        ExecuteCommandInCertainShellHandler.class,
        DfHandler.class,
        UfHandler.class
})
public class ShellVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        WorkerExecutor executor = vertx.createSharedWorkerExecutor("ssh-shell-worker", 10);

        mountHandler(vertx, executor, true);

        Printer.printlnWithLogo("Success start ssh shell verticle.");
    }

}
