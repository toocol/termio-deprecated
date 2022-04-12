package com.toocol.ssh.core.term.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.handler.IHandlerMounter;
import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.core.term.handlers.ListenTerminalSizeHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import jline.Terminal;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:30
 */
@PreloadDeployment
@RegisterHandler(handlers = {
        ListenTerminalSizeHandler.class
})
public class TermVerticle extends AbstractVerticle implements IHandlerMounter {

    public static final Terminal TERMINAL = Terminal.getTerminal();

    @Override
    public void start() throws Exception {
        WorkerExecutor executor = vertx.createSharedWorkerExecutor("ssh-terminal-worker", 1);

        mountHandler(vertx, executor);

        Printer.printlnWithLogo("Success start terminal verticle.");
    }

}
