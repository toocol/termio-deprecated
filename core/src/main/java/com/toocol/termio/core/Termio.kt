package com.toocol.termio.core

import com.toocol.termio.core.shell.core.ShellCharEventDispatcher
import com.toocol.termio.utilities.ansi.Printer.setPrinter
import com.toocol.termio.utilities.log.LoggerFactory
import io.vertx.core.eventbus.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/1 10:13
 */
abstract class Termio : CoroutineScope by MainScope() {
    companion object {
        @JvmStatic
        var eventBus: EventBus? = null

        @JvmField
        @Volatile
        var windowWidth: Int = 0

        @JvmField
        @Volatile
        var windowHeight: Int = 0

        @JvmStatic
        fun eventBus(): EventBus {
            return eventBus!!
        }

        @JvmStatic
        protected fun componentInitialise() {
            LoggerFactory.init()
            ShellCharEventDispatcher.init()
            setPrinter(System.out)
        }
    }
}