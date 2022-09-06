package com.toocol.termio.console.ssh.module

import com.toocol.termio.console.ssh.handlers.BlockingEstablishSshSessionHandler
import com.toocol.termio.core.ssh.handlers.BlockingActiveSshSessionHandler
import com.toocol.termio.utilities.module.AbstractModule
import com.toocol.termio.utilities.module.ModuleDeployment
import com.toocol.termio.utilities.module.RegisterHandler

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/21 22:48
 * @version: 0.0.1
 */
@ModuleDeployment(worker = true, workerPoolSize = 5, workerPoolName = "ssh-worker-pool")
@RegisterHandler(handlers = [BlockingActiveSshSessionHandler::class, BlockingEstablishSshSessionHandler::class])
class SshModule : AbstractModule() {
    @Throws(Exception::class)
    override fun start() {
        mountHandler(vertx, context)
    }
}