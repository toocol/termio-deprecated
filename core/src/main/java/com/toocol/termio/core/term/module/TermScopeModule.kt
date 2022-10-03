package com.toocol.termio.core.term.module

import com.toocol.termio.utilities.module.ScopeModule
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/2 22:33
 * @version: 0.0.1
 */
object TermScopeModule : ScopeModule() {
    @DelicateCoroutinesApi
    override suspend fun start() {
    }

    @DelicateCoroutinesApi
    override suspend fun stop() {
    }
}