package com.toocol.termio.utilities.config

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
        val configures: MutableList<out Configure<out ConfigInstance>> = mutableListOf()
        ConfigureRegister.storage.forEach {
            for (co in it.configures()) configures.add(co.`as`())
        }
        IniConfigLoader.loadConfig(configures.toTypedArray())
    }

    @DelicateCoroutinesApi
    override suspend fun stop() {
    }
}