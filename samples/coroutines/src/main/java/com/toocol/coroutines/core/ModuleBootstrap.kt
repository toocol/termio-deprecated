package com.toocol.coroutines.core

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/2 20:31
 * @version: 0.0.1
 */
abstract class ModuleBootstrap {

    abstract fun array(): Array<out ScopeModule>

    @DelicateCoroutinesApi
    fun bootstrap() {
        array().forEach {
            it.launch(Dispatchers.IO) {
                it.start()
            }
        }
    }

}