package com.toocol.termio.utilities.module

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/2 20:31
 * @version: 0.0.1
 */
interface ModuleBootstrap {

    fun modules(): Array<out ScopeModule>

    @DelicateCoroutinesApi
    fun bootstrap() {
        modules().forEach {
            it.launch(Dispatchers.IO) {
                it.start()
            }
        }
    }

}