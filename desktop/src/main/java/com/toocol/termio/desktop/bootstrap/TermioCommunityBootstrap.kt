package com.toocol.termio.desktop.bootstrap

import com.toocol.termio.core.Termio
import com.toocol.termio.core.cache.MoshSessionCache
import com.toocol.termio.core.cache.STOP_PROGRAM
import com.toocol.termio.core.cache.SshSessionCache
import com.toocol.termio.core.shell.core.Shell
import com.toocol.termio.core.term.TermAddress
import com.toocol.termio.core.term.core.DesktopTermPrinter
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.core.term.core.TermTheme
import com.toocol.termio.desktop.components.executor.ui.CommandExecutor
import com.toocol.termio.desktop.components.terminal.ui.NativeTerminalEmulator
import com.toocol.termio.platform.nativefx.NativeBinding
import com.toocol.termio.utilities.ansi.Printer.println
import com.toocol.termio.utilities.config.IniConfigLoader
import com.toocol.termio.utilities.console.Console
import com.toocol.termio.utilities.functional.Ignore
import com.toocol.termio.utilities.log.FileAppender.close
import com.toocol.termio.utilities.utils.MessageBox
import java.util.*
import java.util.concurrent.TimeUnit
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
        IniConfigLoader.setConfigFileRootPath("/config")
        IniConfigLoader.setConfigurePaths(arrayOf("com.toocol.termio.desktop"))
        componentInitialise()

        Term.theme = TermTheme.LIGHT_THEME
        Term.registerConsole(Console.get())
        Term.initializeReader(CommandExecutor.executorReaderInputStream)
        Shell.initializeReader(NativeTerminalEmulator.terminalReaderInputStream)
        DesktopTermPrinter.registerPrintStream(CommandExecutor.commandExecutorPrintStream)

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
        NativeBinding.init()

        watchStart()
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

    private fun watchStart() {
        Thread ({
            try {
                val ret = initialLatch!!.await(30, TimeUnit.SECONDS)
                if (!ret) {
                    throw RuntimeException("Waiting timeout.")
                }
                loadingLatch = null
                initialLatch = null
                verticleClassList = null
                vertx!!.eventBus().send(TermAddress.ACCEPT_COMMAND.address(), null)
                logger.info("Bootstrap Termio community success.")
            } catch (e: Exception) {
                logger.error("Bootstrap Termio community failed.")
                exitProcess(-1)
            }
        }, "bootstrap-watch-thread").start()
    }
}