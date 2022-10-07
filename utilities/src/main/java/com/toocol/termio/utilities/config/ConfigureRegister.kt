package com.toocol.termio.utilities.config

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/7 12:27
 * @version: 0.0.1
 */
abstract class ConfigureRegister {
    init {
        register()
    }

    private fun register() {
        storage.add(this)
    }

    abstract fun configures(): Array<out Configure<out ConfigInstance>>

    fun reg() {}

    companion object {
        internal val storage: MutableList<ConfigureRegister> = mutableListOf()
    }
}