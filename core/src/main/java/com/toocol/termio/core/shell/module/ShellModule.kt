package com.toocol.termio.core.shell.module

import com.toocol.termio.core.shell.handlers.*
import com.toocol.termio.utilities.module.AbstractModule
import com.toocol.termio.utilities.module.ModuleDeployment
import com.toocol.termio.utilities.module.RegisterHandler

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/21 22:48
 * @version: 0.0.1
 */
@ModuleDeployment(worker = true, workerPoolName = "shell-worker-pool")
@RegisterHandler(handlers = [
    BlockingShellDisplayHandler::class,
    BlockingShellExecuteHandler::class,
    BlockingExecuteSingleCmdHandler::class,
    BlockingExecuteCmdInShellHandler::class,
    BlockingDfHandler::class,
    BlockingUfHandler::class
])
class ShellModule : AbstractModule() {
    @Throws(Exception::class)
    override fun start() {
        mountHandler(vertx, context)
    }
}