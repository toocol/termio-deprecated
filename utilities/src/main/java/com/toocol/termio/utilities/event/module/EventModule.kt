package com.toocol.termio.utilities.event.module

import com.toocol.termio.utilities.event.core.EventDispatcher
import com.toocol.termio.utilities.event.core.EventListenerContainer
import com.toocol.termio.utilities.event.handlers.AsyncEventHandler
import com.toocol.termio.utilities.module.AbstractModule
import com.toocol.termio.utilities.module.ModuleDeployment
import com.toocol.termio.utilities.module.RegisterHandler

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 15:17
 * @version: 0.0.1
 */
@ModuleDeployment(worker = true, workerPoolName = "event-worker-pool")
@RegisterHandler(handlers = [
    AsyncEventHandler::class
])
class EventModule: AbstractModule() {

    override fun start() {
        EventDispatcher.register(vertx.eventBus())
        EventListenerContainer.init()
        mountHandler(vertx, context)
    }

}