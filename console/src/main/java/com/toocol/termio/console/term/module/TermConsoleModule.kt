package com.toocol.termio.console.term.module

import com.toocol.termio.console.term.handlers.BlockingAcceptCommandHandler
import com.toocol.termio.console.term.handlers.BlockingMonitorTerminalHandler
import com.toocol.termio.console.term.handlers.ExecuteCommandHandler
import com.toocol.termio.utilities.module.AbstractModule
import com.toocol.termio.utilities.module.ModuleDeployment
import com.toocol.termio.utilities.module.RegisterHandler

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:30
 */
@ModuleDeployment(weight = 10, worker = true, workerPoolName = "term-console-worker-pool")
@RegisterHandler(handlers = [BlockingMonitorTerminalHandler::class, BlockingAcceptCommandHandler::class, ExecuteCommandHandler::class])
class TermConsoleModule : AbstractModule() {
    @Throws(Exception::class)
    override fun start() {
        mountHandler(vertx, context)
    }
}