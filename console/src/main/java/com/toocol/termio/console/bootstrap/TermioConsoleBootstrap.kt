package com.toocol.termio.console.bootstrap

import com.toocol.termio.core.Termio
import com.toocol.termio.core.cache.FIRST_IN
import com.toocol.termio.core.cache.MoshSessionCache
import com.toocol.termio.core.cache.STOP_PROGRAM
import com.toocol.termio.core.cache.SshSessionCache
import com.toocol.termio.core.shell.core.Shell
import com.toocol.termio.core.term.TermAddress
import com.toocol.termio.core.term.core.ConsoleTermPrinter.Companion.registerPrintStream
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.utilities.ansi.Printer
import com.toocol.termio.utilities.ansi.Printer.clear
import com.toocol.termio.utilities.ansi.Printer.printErr
import com.toocol.termio.utilities.ansi.Printer.printLoading
import com.toocol.termio.utilities.ansi.Printer.println
import com.toocol.termio.utilities.config.IniConfigLoader
import com.toocol.termio.utilities.functional.Ignore
import com.toocol.termio.utilities.log.FileAppender.close
import com.toocol.termio.utilities.utils.MessageBox
import sun.misc.Signal
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/16 14:44
 */
object TermioConsoleBootstrap : Termio() {
    @JvmStatic
    fun runConsole(runClass: Class<*>) {
        runType = RunType.CONSOLE
        System.setProperty("logFile", "termio-console.log")
        /* Block the Ctrl+C */
        Signal.handle(Signal("INT")) { }
        componentInitialise()

        Term.initializeReader(System.`in`)
        Shell.initializeReader(System.`in`)
        registerPrintStream(System.out)

        IniConfigLoader.setConfigFileRootPath("/config")
        IniConfigLoader.setConfigurePaths(arrayOf("com.toocol.termio.core.config.core"))
        printLoading(loadingLatch!!)

        vertx = prepareVertxEnvironment(
            Optional.ofNullable(runClass.getAnnotation(
                Ignore::class.java))
                .map { ignore: Ignore ->
                    ignore.ignore
                        .asSequence()
                        .map { ig -> ig.java }
                        .toSet()
                }
                .orElse(null)
        )
        eventBus = vertx!!.eventBus()
        addShutdownHook()
        waitingStartConsole()
    }

    private fun waitingStartConsole() {
        try {
            val ret = initialLatch!!.await(30, TimeUnit.SECONDS)
            if (!ret) {
                throw RuntimeException("Waiting timeout.")
            }
            while (true) {
                if (Printer.LOADING_ACCOMPLISH) {
                    loadingLatch!!.await()
                    vertx!!.eventBus().send(TermAddress.MONITOR_TERMINAL.address(), null)
                    vertx!!.eventBus().send(TermAddress.ACCEPT_COMMAND_CONSOLE.address(), FIRST_IN)
                    loadingLatch = null
                    initialLatch = null
                    verticleClassList = null
                    System.gc()
                    logger.info("Start termio success.")
                    break
                }
            }
        } catch (e: Exception) {
            vertx!!.close()
            MessageBox.setExitMessage("Termio start up error.")
            exitProcess(-1)
        }
    }

    private fun addShutdownHook() {
        /* Add shutdown hook */
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                clear()
                STOP_PROGRAM = true
                if (MessageBox.hasExitMessage()) {
                    println(MessageBox.exitMessage())
                }
                println("Termio: shutdown")
                SshSessionCache.stopAll()
                MoshSessionCache.stopAll()
                close()
                vertx!!.close()
            } catch (e: Exception) {
                printErr("Failed to execute shutdown hook.")
            }
        })
    }
}