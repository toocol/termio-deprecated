package com.toocol.ssh.core.shell.vert;

import com.jcraft.jsch.JSch;
import com.toocol.ssh.common.annotation.FinalDeployment;
import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.handler.IHandlerMounter;
import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.common.utils.SnowflakeGuidGenerator;
import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.shell.handlers.AcceptShellCmdHandler;
import com.toocol.ssh.core.shell.handlers.EstablishSessionChannelHandler;
import com.toocol.ssh.core.shell.handlers.ExhibitShellHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:30
 */
@FinalDeployment
@RegisterHandler(handlers = {
        EstablishSessionChannelHandler.class,
        ExhibitShellHandler.class,
        AcceptShellCmdHandler.class,
})
public class ShellVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        WorkerExecutor executor = vertx.createSharedWorkerExecutor("ssh-shell-worker", 3);

        final SessionCache sessionCache = new SessionCache();
        mountHandler(vertx, executor, true, new JSch(), new SnowflakeGuidGenerator(), sessionCache);

        Runtime.getRuntime().addShutdownHook(new Thread(sessionCache::stopAll));

        Printer.printlnWithLogo("Success start ssh verticle.");
    }

}
