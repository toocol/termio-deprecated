package com.toocol.termio.utilities.module

import com.toocol.termio.utilities.utils.MessageBox
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import org.jetbrains.annotations.NotNull
import kotlin.system.exitProcess

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/27 10:40
 */
abstract class NonBlockingMessageHandler protected constructor(vertx: Vertx, context: Context)
    : AbstractMessageHandler(vertx, context) {

    override fun <T> handle(message: Message<T>) {
        try {
            handleInline(message)
        } catch (e: Exception) {
            MessageBox.setExitMessage("Caught exception, exit program, message = " + e.message)
            error("Caught exception, exit program, stackTrace : {}", parseStackTrace(e))
            exitProcess(-1)
        }
    }

    abstract fun <T> handleInline(@NotNull message: Message<T>)
}