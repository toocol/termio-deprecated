package com.toocol.termio.utilities.module

import com.toocol.termio.utilities.log.Loggable
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/2 20:31
 * @version: 0.0.1
 */
interface ModuleBootstrap : Loggable{

    fun modules(): Array<out ScopeModule>

    @DelicateCoroutinesApi
    suspend fun bootstrap() {
        modules().forEach {
            it.launch(Dispatchers.IO) {
                try {
                    it.start()
                } catch (e: Exception) {
                    error("Initialize scope module failed, module = ${it.javaClass.name}, e = ${e.javaClass.name}")
                }
            }
        }
    }

}