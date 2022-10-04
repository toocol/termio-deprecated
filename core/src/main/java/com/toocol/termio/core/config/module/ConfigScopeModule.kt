package com.toocol.termio.core.config.module

import com.toocol.termio.utilities.config.IniConfigLoader
import com.toocol.termio.utilities.module.ScopeModule
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/3 23:28
 * @version: 0.0.1
 */
object ConfigScopeModule : ScopeModule(){
    @DelicateCoroutinesApi
    override suspend fun start() {
        IniConfigLoader.loadConfig()
    }

    @DelicateCoroutinesApi
    override suspend fun stop() {
    }
}