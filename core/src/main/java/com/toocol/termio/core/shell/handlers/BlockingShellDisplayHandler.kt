package com.toocol.termio.core.shell.handlers

import com.toocol.termio.core.cache.*
import com.toocol.termio.core.shell.ShellAddress
import com.toocol.termio.core.shell.api.ShellCmdApi
import com.toocol.termio.utilities.ansi.Printer.print
import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.module.BlockingMessageHandler
import com.toocol.termio.utilities.module.IAddress
import com.toocol.termio.utilities.sync.SharedCountdownLatch.countdown
import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import java.nio.charset.StandardCharsets

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 15:44
 */
class BlockingShellDisplayHandler(vertx: Vertx?, context: Context?, parallel: Boolean) : BlockingMessageHandler<Long?>(
    vertx!!, context!!, parallel), Loggable {

    private val sshSessionCache = SshSessionCache.Instance
    private val shellCache = ShellCache.Instance

    @Volatile
    private var cmdHasFeedbackWhenJustExit = false

    @Volatile
    private var firstIn: Long = 0
    override fun consume(): IAddress {
        return ShellAddress.DISPLAY_SHELL
    }

    @Throws(Exception::class)
    override fun <T> handleBlocking(promise: Promise<Long?>, message: Message<T>) {
        val sessionId = cast<Long>(message.body())
        val shell = shellCache.getShell(sessionId)
        if (shell!!.hasWelcome() && SHOW_WELCOME) {
            shell.printWelcome()
            SHOW_WELCOME = false
        }
        if (ACCESS_EXHIBIT_SHELL_WITH_PROMPT) {
            print(shell.getPrompt())
        } else {
            ACCESS_EXHIBIT_SHELL_WITH_PROMPT = true
        }

        /*
         * All the remote feedback data is getting from this InputStream.
         * And don't know why, there should get a new InputStream from channelShell.
         **/
        val inputStream = shell.getInputStream()
        val tmp = ByteArray(1024)
        while (true) {
            while (inputStream!!.available() > 0) {
                if (EXHIBIT_WAITING_BEFORE_COMMAND_PREPARE) {
                    continue
                }
                val i = inputStream.read(tmp, 0, 1024)
                if (i < 0) {
                    break
                }
                val msg = String(tmp, 0, i, StandardCharsets.UTF_8)
                val hasPrint = shell.print(msg)
                if (hasPrint && JUST_CLOSE_EXHIBIT_SHELL) {
                    cmdHasFeedbackWhenJustExit = true
                }
                countdown(BlockingShellExecuteHandler::class.java, this.javaClass)
            }
            if (HANGED_QUIT) {
                if (inputStream.available() > 0) {
                    continue
                }
                break
            }
            if (shell.isClosed) {
                if (inputStream.available() > 0) {
                    continue
                }
                break
            }
            if (JUST_CLOSE_EXHIBIT_SHELL) {
                if (firstIn == 0L) {
                    firstIn = System.currentTimeMillis()
                } else {
                    if (System.currentTimeMillis() - firstIn >= 2000) {
                        if (inputStream.available() > 0) {
                            continue
                        }
                        firstIn = 0
                        break
                    }
                }
                if (cmdHasFeedbackWhenJustExit) {
                    if (inputStream.available() > 0) {
                        continue
                    }
                    firstIn = 0
                    break
                }
            }
            /*
             * Reduce CPU utilization
             */Thread.sleep(1)
        }
        promise.complete(sessionId)
    }

    @Throws(Exception::class)
    override fun <T> resultBlocking(asyncResult: AsyncResult<Long?>, message: Message<T>) {
        if (JUST_CLOSE_EXHIBIT_SHELL) {
            JUST_CLOSE_EXHIBIT_SHELL = false
            cmdHasFeedbackWhenJustExit = false
            countdown(ShellCmdApi.Lock::class.java, this.javaClass)
            return
        }
        if (ACCEPT_SHELL_CMD_IS_RUNNING) {
            val sessionId = asyncResult.result()
            val channelShell = sshSessionCache.getChannelShell(sessionId!!)
            if (channelShell != null && !channelShell.isClosed) {
                eventBus.send(ShellAddress.DISPLAY_SHELL.address(), sessionId)
            }
        }
    }
}