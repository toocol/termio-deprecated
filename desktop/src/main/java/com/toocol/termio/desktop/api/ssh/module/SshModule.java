package com.toocol.termio.desktop.api.ssh.module;

import com.toocol.termio.core.ssh.handlers.BlockingActiveSshSessionHandler;
import com.toocol.termio.core.ssh.handlers.AbstractBlockingEstablishSshSessionHandler;
import com.toocol.termio.desktop.api.ssh.handlers.BlockingEstablishSshSessionHandler;
import com.toocol.termio.utilities.module.AbstractModule;
import com.toocol.termio.utilities.module.ModuleDeployment;
import com.toocol.termio.utilities.module.RegisterHandler;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/21 22:48
 * @version: 0.0.1
 */
@ModuleDeployment(worker = true, workerPoolSize = 5, workerPoolName = "ssh-worker-pool")
@RegisterHandler(handlers = {
        BlockingActiveSshSessionHandler.class,
        BlockingEstablishSshSessionHandler.class,
})
public final class SshModule extends AbstractModule {

    @Override
    public void start() throws Exception {
        mountHandler(vertx, context);
    }

}
