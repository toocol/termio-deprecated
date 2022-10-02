package com.toocol.coroutines.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.MainScope

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/2 19:34
 * @version: 0.0.1
 */
abstract class ScopeModule : CoroutineScope by MainScope() {

    @DelicateCoroutinesApi
    abstract suspend fun start()

    @DelicateCoroutinesApi
    abstract suspend fun stop()

}
