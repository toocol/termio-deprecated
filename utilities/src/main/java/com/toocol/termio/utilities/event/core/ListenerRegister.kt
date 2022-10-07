package com.toocol.termio.utilities.event.core

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/7 11:52
 * @version: 0.0.1
 */
abstract class ListenerRegister {
    init {
        register()
    }

    private fun register() {
        storage.add(this)
    }

    abstract fun listeners(): Array<out EventListener<out AbstractEvent>>

    fun reg() {}

    companion object {
        internal val storage: MutableList<ListenerRegister> = mutableListOf()
    }
}