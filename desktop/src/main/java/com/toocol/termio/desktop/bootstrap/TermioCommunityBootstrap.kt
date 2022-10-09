package com.toocol.termio.desktop.bootstrap

import com.toocol.termio.core.Termio
import com.toocol.termio.core.auth.module.AuthScopeModule
import com.toocol.termio.core.cache.MoshSessionCache
import com.toocol.termio.core.cache.STOP_PROGRAM
import com.toocol.termio.core.cache.SshSessionCache
import com.toocol.termio.core.shell.core.Shell
import com.toocol.termio.core.term.core.DesktopTermPrinter
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.core.term.core.TermTheme
import com.toocol.termio.desktop.components.executor.ui.CommandExecutor
import com.toocol.termio.desktop.components.nativeTerminalEmulator
import com.toocol.termio.platform.nativefx.NativeBinding
import com.toocol.termio.utilities.ansi.Printer.println
import com.toocol.termio.utilities.config.ConfigScopeModule
import com.toocol.termio.utilities.config.IniConfigLoader
import com.toocol.termio.utilities.event.module.EventScopeModule
import com.toocol.termio.utilities.log.FileAppender.close
import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.module.ModuleBootstrap
import com.toocol.termio.utilities.module.ScopeModule
import com.toocol.termio.utilities.utils.MessageBox
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/2 0:34
 * @version: 0.0.1
 */
object TermioCommunityBootstrap : Termio(), ModuleBootstrap, Loggable {
    @OptIn(DelicateCoroutinesApi::class)
    @JvmStatic
    fun run() {
        launch(Dispatchers.Default) {
            Term.theme = TermTheme.LIGHT_THEME
            Shell.initializeReader(nativeTerminalEmulator.terminalReaderInputStream)
            DesktopTermPrinter.registerPrintStream(CommandExecutor.commandExecutorPrintStream)

            System.setProperty("logFile", "termio-desktop.log")
            IniConfigLoader.setConfigFileRootPath("/config")
            componentInitialise()

            bootstrap()
            NativeBinding.init()

            info("Bootstrap Termio community success.")
        }
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
    }

    override fun modules(): Array<out ScopeModule> {
        return arrayOf(
            ConfigScopeModule,
            EventScopeModule,
            AuthScopeModule,
        )
    }
}