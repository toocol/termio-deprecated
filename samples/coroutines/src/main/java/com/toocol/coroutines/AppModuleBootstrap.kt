package com.toocol.coroutines

import com.toocol.coroutines.core.ModuleBootstrap
import com.toocol.coroutines.core.ScopeModule

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/2 21:27
 * @version: 0.0.1
 */
object AppModuleBootstrap : ModuleBootstrap(){
    override fun array(): Array<out ScopeModule> {
        return arrayOf(
            UserModule
        )
    }
}