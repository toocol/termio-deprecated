package com.toocol.termio.utilities.config

import com.toocol.termio.utilities.utils.Asable

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/28 23:08
 * @version: 0.0.1
 */
abstract class ConfigInstance : Asable {

    private val instance: ConfigInstance

    abstract fun clazz(): Class<out ConfigInstance>

    fun <T : ConfigInstance> get(): T {
        return instance.`as`()
    }

    init {
        val constructor = this.clazz().getDeclaredConstructor()
        constructor.trySetAccessible()
        instance = constructor.newInstance()
    }
}