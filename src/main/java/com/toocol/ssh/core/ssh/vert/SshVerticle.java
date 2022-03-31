package com.toocol.ssh.core.ssh.vert;

import com.jcraft.jsch.JSch;
import com.toocol.ssh.common.annotation.FinalDeployment;
import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.handler.IHandlerMounter;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.common.utils.SnowflakeGuidGenerator;
import com.toocol.ssh.core.ssh.handlers.AcceptShellCmdHandler;
import com.toocol.ssh.core.ssh.handlers.ConnectChannelShellHandler;
import com.toocol.ssh.core.ssh.handlers.EstablishSshSessionHandler;
import com.toocol.ssh.core.ssh.handlers.ExhibitShellHandler;
import com.toocol.ssh.core.ssh.cache.SessionCache;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:30
 */
@FinalDeployment
@RegisterHandler(handlers = {
        EstablishSshSessionHandler.class,
        ConnectChannelShellHandler.class,
        ExhibitShellHandler.class,
        AcceptShellCmdHandler.class,
})
public class SshVerticle extends AbstractVerticle implements IHandlerMounter {

    @Override
    public void start() throws Exception {
        WorkerExecutor executor = vertx.createSharedWorkerExecutor("ssh-session-worker", 20);

        final SessionCache sessionCache = new SessionCache();
        mountHandler(vertx, executor, true, new JSch(), new SnowflakeGuidGenerator(), sessionCache);

        Runtime.getRuntime().addShutdownHook(new Thread(sessionCache::stopAll));

        PrintUtil.println("Success start ssh verticle.");
    }

}
