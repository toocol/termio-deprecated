package com.toocol.termio.utilities.module

import com.toocol.termio.utilities.execeptions.IStacktraceParser
import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.Castable
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import org.jetbrains.annotations.NotNull

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 10:34
 */
abstract class AbstractMessageHandler protected constructor(
    @JvmField
    // the vertx system object.
    protected val vertx: Vertx,
    @JvmField
    // the context of verticle.
    protected val context: Context,
) : Castable, Loggable, IStacktraceParser {

    /**
     * the event bus of Vert.x
     */
    @JvmField
    protected val eventBus: EventBus = vertx.eventBus()

    /**
     * handle the message event
     *
     * @param message message event
     * @param <T>     generic type
    </T> */
    abstract fun <T> handle(message: Message<T>)

    /**
     * return the address that handler handle of.
     *
     * @return address
     */
    @NotNull
    abstract fun consume(): IAddress
}