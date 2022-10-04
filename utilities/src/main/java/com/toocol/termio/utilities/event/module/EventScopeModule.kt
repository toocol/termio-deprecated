package com.toocol.termio.utilities.event.module

import com.toocol.termio.utilities.event.core.EventListenerContainer
import com.toocol.termio.utilities.module.ScopeModule
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/3 23:02
 * @version: 0.0.1
 */
object EventScopeModule : ScopeModule() {
    @DelicateCoroutinesApi
    override suspend fun start() {
        EventListenerContainer.init()
    }

    @DelicateCoroutinesApi
    override suspend fun stop() {
    }
}