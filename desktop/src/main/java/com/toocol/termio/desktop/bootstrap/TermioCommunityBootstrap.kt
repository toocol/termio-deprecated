package com.toocol.termio.desktop.bootstrap

import com.toocol.termio.core.Termio
import com.toocol.termio.core.cache.MoshSessionCache
import com.toocol.termio.core.cache.STOP_PROGRAM
import com.toocol.termio.core.cache.SshSessionCache
import com.toocol.termio.core.shell.core.Shell
import com.toocol.termio.core.term.TermAddress
import com.toocol.termio.core.term.core.DesktopTermPrinter.Companion.registerPrintStream
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.desktop.ui.executor.CommandExecutorPanel
import com.toocol.termio.desktop.ui.terminal.DesktopConsole
import com.toocol.termio.desktop.ui.terminal.DesktopTerminalPanel
import com.toocol.termio.utilities.ansi.Printer
import com.toocol.termio.utilities.ansi.Printer.println
import com.toocol.termio.utilities.config.IniConfigLoader
import com.toocol.termio.utilities.console.Console
import com.toocol.termio.utilities.functional.Ignore
import com.toocol.termio.utilities.log.FileAppender.close
import com.toocol.termio.utilities.utils.MessageBox
import java.util.*
import kotlin.system.exitProcess

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/2 0:34
 * @version: 0.0.1
 */
object TermioCommunityBootstrap : Termio() {
    @JvmStatic
    fun runDesktop(runClass: Class<*>) {
        runType = RunType.DESKTOP
        System.setProperty("logFile", "termio-desktop.log")
        Console.setConsole(DesktopConsole())
        IniConfigLoader.setConfigFileRootPath("/config")
        IniConfigLoader.setConfigurePaths(arrayOf("com.toocol.termio.desktop.configure"))
        componentInitialise()

        Term.initializeReader(CommandExecutorPanel.executorReaderInputStream)
        Shell.initializeReader(DesktopTerminalPanel.terminalReaderInputStream)
        registerPrintStream(CommandExecutorPanel.commandExecutorPrintStream)

        loadingLatch!!.countDown()
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
        waitingStartDesktop()
    }

    @JvmStatic
    fun stop() {
        STOP_PROGRAM = true
        if (MessageBox.hasExitMessage()) {
            println(MessageBox.exitMessage())
        }
        SshSessionCache.stopAll()
        MoshSessionCache.stopAll()
        close()
        vertx!!.close()
    }

    private fun waitingStartDesktop() {
        try {
            while (true) {
                if (Printer.LOADING_ACCOMPLISH) {
                    vertx!!.eventBus().send(TermAddress.ACCEPT_COMMAND.address(), null)
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
}