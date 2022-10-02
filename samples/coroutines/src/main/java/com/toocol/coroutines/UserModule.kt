package com.toocol.coroutines

import com.toocol.coroutines.core.ScopeModule
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/2 19:54
 * @version: 0.0.1
 */
object UserModule : ScopeModule() {
    @DelicateCoroutinesApi
    override suspend fun start() {
    }

    @DelicateCoroutinesApi
    override suspend fun stop() {
    }
}