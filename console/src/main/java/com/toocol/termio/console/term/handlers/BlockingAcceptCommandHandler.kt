package com.toocol.termio.console.term.handlers

import com.toocol.termio.core.cache.*
import com.toocol.termio.core.term.TermAddress
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.core.term.core.Term.Companion.promptLen
import com.toocol.termio.utilities.ansi.Printer.clear
import com.toocol.termio.utilities.module.BlockingMessageHandler
import com.toocol.termio.utilities.module.IAddress
import com.toocol.termio.utilities.utils.MessageBox
import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:11
 */
class BlockingAcceptCommandHandler(vertx: Vertx?, context: Context?, parallel: Boolean) :
    BlockingMessageHandler<Boolean?>(
        vertx!!, context!!, parallel) {
    private val term = Term.instance
    override fun consume(): IAddress {
        return TermAddress.ACCEPT_COMMAND
    }

    override fun <T> handleBlocking(promise: Promise<Boolean?>, message: Message<T>) {
        try {
            val signal = cast<Int>(message.body())
            if (signal == NORMAL_BACK || signal == FIRST_IN || signal == CONNECT_FAILED) {
                clear()
                Term.instance.printScene(false)
            }
            term.printExecuteBackground()
            if (signal == CONNECT_FAILED) {
                term.printErr("lost connection.")
            }
            if (MessageBox.hasMessage()) {
                term.printDisplay(MessageBox.message())
                MessageBox.clearMessage()
            }
            if (MessageBox.hasErrorMessage()) {
                term.printErr(MessageBox.errorMessage())
                MessageBox.clearErrorMessage()
            }
            term.showCursor()
            while (true) {
                term.setCursorPosition(promptLen, Term.executeLine)
                val cmd = term.readLine()
                val latch = CountDownLatch(1)
                val isBreak = AtomicBoolean()
                eventBus.request(TermAddress.EXECUTE_OUTSIDE.address(), cmd) { result: AsyncResult<Message<Any?>> ->
                    isBreak.set(cast(result.result().body()))
                    latch.countDown()
                }
                latch.await()
                if (isBreak.get()) {
                    // start to accept shell's command, break the cycle.
                    promise.complete(false)
                    break
                }
                if (STOP_ACCEPT_OUT_COMMAND) {
                    STOP_ACCEPT_OUT_COMMAND = false
                    promise.complete(false)
                    break
                }
            }
        } catch (e: Exception) {
            // to do nothing, enter the next round of accept command cycle
            promise.complete(true)
        }
    }

    override fun <T> resultBlocking(asyncResult: AsyncResult<Boolean?>, message: Message<T>) {
        val result = asyncResult.result()
        result?: return
        if (result) {
            eventBus.send(TermAddress.ACCEPT_COMMAND.address(), ACCEPT_ERROR)
        }
    }
}