package com.toocol.termio.console.term.handlers

import io.vertx.core.Vertx
import com.toocol.termio.utilities.module.NonBlockingMessageHandler
import com.toocol.termio.utilities.module.IAddress
import com.toocol.termio.core.term.TermAddress
import com.toocol.termio.utilities.utils.StrUtil
import io.vertx.core.Context
import io.vertx.core.eventbus.Message

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:09
 */
class CleanEchoBufferHandler(vertx: Vertx?, context: Context?) : NonBlockingMessageHandler(
    vertx!!, context!!) {
    override fun consume(): IAddress {
        return TermAddress.TERMINAL_ECHO_CLEAN_BUFFER
    }

    override fun <T> handleInline(message: Message<T>) {
        DynamicEchoHandler.lastInput = StrUtil.EMPTY
    }
}