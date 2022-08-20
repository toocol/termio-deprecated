package com.toocol.termio.utilities.module

import com.toocol.termio.utilities.utils.MessageBox
import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import org.jetbrains.annotations.NotNull
import kotlin.system.exitProcess

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 10:46
 */
abstract class BlockingMessageHandler<R>(
    vertx: Vertx,
    context: Context,
    // whether the handler is handle parallel
    private val parallel: Boolean
) : AbstractMessageHandler(vertx, context) {

    /**
     * handle the message event
     *
     * @param message message event
     * @param <T>     generic type
    </T> */
    override fun <T> handle(message: Message<T>) {
        context.executeBlocking(
            { promise: Promise<Any?> ->
                try {
                    handleBlocking(cast(promise), message)
                } catch (e: Exception) {
                    MessageBox.setExitMessage("Caught exception, exit program, message = " + e.message)
                    error("Caught exception, exit program, stackTrace : {}", parseStackTrace(e))
                    exitProcess(-1)
                }
            },
            !parallel
        ) { asyncResult: AsyncResult<Any?> ->
            try {
                resultBlocking(cast(asyncResult), message)
            } catch (e: Exception) {
                MessageBox.setExitMessage("Caught exception, exit program, message = " + e.message)
                error("Caught exception, exit program, stackTrace : {}", parseStackTrace(e))
                exitProcess(-1)
            }
        }
    }

    /**
     * execute the blocked process
     *
     * @param promise promise
     * @param message message
     * @param <T>     generic type
     * @throws Exception exception
    </T> */
    @Throws(Exception::class)
    protected abstract fun <T> handleBlocking(@NotNull promise: Promise<R>, @NotNull message: Message<T>)

    /**
     * response the blocked process result
     *
     * @param asyncResult async result
     * @param message     message
     * @param <T>         generic type
     * @throws Exception exception
    </T> */
    @Throws(Exception::class)
    protected abstract fun <T> resultBlocking(@NotNull asyncResult: AsyncResult<R>, @NotNull message: Message<T>)
}