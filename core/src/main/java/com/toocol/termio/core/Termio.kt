package com.toocol.termio.core

import com.toocol.termio.core.shell.core.ShellCharEventDispatcher
import com.toocol.termio.core.term.core.TermCharEventDispatcher
import com.toocol.termio.utilities.ansi.Printer.setPrinter
import com.toocol.termio.utilities.jni.JNILoader
import com.toocol.termio.utilities.log.LoggerFactory
import com.toocol.termio.utilities.log.LoggerFactory.getLogger
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/1 10:13
 */
abstract class Termio : CoroutineScope by MainScope() {
    enum class RunType {
        CONSOLE, DESKTOP
    }

    companion object {
        protected const val blockedCheckInterval = 30 * 24 * 60 * 60 * 1000L

        @JvmStatic
        val logger = getLogger(Termio::class.java)

        @JvmStatic
        var runType: RunType? = null

        @JvmStatic
        var vertx: Vertx? = null

        @JvmStatic
        var eventBus: EventBus? = null

        @JvmField
        @Volatile
        var windowWidth: Int = 0

        @JvmField
        @Volatile
        var windowHeight: Int = 0

        @JvmStatic
        fun runType(): RunType {
            return runType!!
        }

        @JvmStatic
        fun vertx(): Vertx {
            return vertx!!
        }

        @JvmStatic
        fun eventBus(): EventBus {
            return eventBus!!
        }

        @JvmStatic
        protected fun componentInitialise() {
            if (runType == RunType.CONSOLE) {
                JNILoader.load()
            }
            LoggerFactory.init()
            TermCharEventDispatcher.init()
            ShellCharEventDispatcher.init()
            setPrinter(System.out)
        }
    }
}