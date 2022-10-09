package com.toocol.termio.desktop.components.terminal.ui

import com.toocol.termio.desktop.components.panel.ui.TopMenuPanel
import com.toocol.termio.desktop.components.sidebar.ui.BottomStatusBar
import com.toocol.termio.platform.console.MetadataPrinterOutputStream
import com.toocol.termio.platform.console.MetadataReaderInputStream
import com.toocol.termio.platform.console.TerminalConsolePrintStream
import com.toocol.termio.platform.nativefx.NativeBinding
import com.toocol.termio.platform.nativefx.NativeBinding.SharedStringType
import com.toocol.termio.platform.nativefx.NativeNode
import com.toocol.termio.platform.window.WindowSizeAdjuster
import com.toocol.termio.utilities.ansi.Printer
import com.toocol.termio.utilities.log.Loggable
import javafx.beans.value.ObservableValue
import javafx.scene.layout.Pane

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/18 18:35
 * @version: 0.0.1
 */
class NativeTerminalEmulator : NativeNode(true, true) {
    /**
     * Each DesktopTerminalPanel or CommandExecutorPanel has one onw MetadataPrinterOutputStream and PrintStream correspondent:
     * Feedback data.
     */
    private val terminalWriterOutputStream = MetadataPrinterOutputStream()
    private val terminalPrintStream = TerminalConsolePrintStream(terminalWriterOutputStream)
    private val terminalOutputService = TerminalOutputService()
    /**
     * DesktopTerminalPanel has only one MetadataReaderInputStream:
     * Get user's input data.
     */
    @JvmField
    val terminalReaderInputStream = MetadataReaderInputStream()

    override fun initialize() {
        apply {
            connect("_emulator_mem")

            addNativeEventListener { key, type, evt ->
                if (WindowSizeAdjuster.onMoved && updateOnce) {
                    println("> key: $key, type: $type, evt_msg: $evt")
                    updateNativeImage()
                    updateOnce = false
                }
            }

            terminalOutputService.start()

            focusedProperty()
                .addListener { _: ObservableValue<out Boolean>?, ov: Boolean, nv: Boolean ->
                    if (nv) {
                        activeTerminal()
                    }
                    if (ov != nv) {
                        NativeBinding.requestFocus(key, nv, System.nanoTime())
                    }
                }
        }
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run {
            prefWidthProperty().bind(major.widthProperty().multiply(widthRatio))
        }
        heightRatio?.run {
            prefHeightProperty().bind(major.heightProperty()
                .subtract(TopMenuPanel.fixedHeight + BottomStatusBar.fixedHeight).multiply(heightRatio))
        }
    }

    override fun styleClasses(): Array<String> {
        return arrayOf()
    }

    override fun actionAfterShow() {}

    fun activeTerminal() {
        Printer.setPrinter(terminalPrintStream)
    }

    fun createSshSession(sessionId: Long, host: String, user: String, password: String) {
        NativeBinding.createSshSession(key, sessionId, host, user, password, System.nanoTime())
    }

    inner class TerminalOutputService : Loggable {
        fun start() {
            val thread = Thread({
                while (true) {
                    try {
                        if (terminalWriterOutputStream.available() > 0) {
                            val text = terminalWriterOutputStream.read()
                            NativeBinding.sendMsg(key, text, SharedStringType.NFX_SEND_TEXT.type)
                            terminalPrintStream.signal()
                        }
                        Thread.sleep(1)
                    } catch (e: Exception) {
                        warn("TerminalOutputService catch exception, e = ${e.javaClass.name}, msg = ${e.message}")
                    }
                }
            }, "terminal-output-service")
            thread.isDaemon = true
            thread.start()
        }
    }
}