package com.toocol.termio.desktop.components.terminal.ui

import com.toocol.termio.desktop.components.panel.ui.TopMenuPanel
import com.toocol.termio.desktop.components.sidebar.ui.BottomStatusBar
import com.toocol.termio.desktop.components.sidebar.ui.TopSessionTabBar
import com.toocol.termio.platform.console.MetadataPrinterOutputStream
import com.toocol.termio.platform.console.MetadataReaderInputStream
import com.toocol.termio.platform.console.TerminalConsolePrintStream
import com.toocol.termio.platform.nativefx.NativeBinding
import com.toocol.termio.platform.nativefx.NativeBinding.SharedStringType
import com.toocol.termio.platform.nativefx.NativeNode
import com.toocol.termio.platform.window.WindowSizeAdjuster
import com.toocol.termio.utilities.ansi.Printer
import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.CharUtil
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Pane
import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/18 18:35
 * @version: 0.0.1
 */
class NativeTerminalEmulator(id: Long, sessionId: Long) : NativeNode(id, pixelBufferEnabled = true) {
    /**
     * Each DesktopTerminalPanel or CommandExecutorPanel has one onw MetadataPrinterOutputStream and PrintStream correspondent:
     * Feedback data.
     */
    private val terminalWriterOutputStream = MetadataPrinterOutputStream()
    private val terminalPrintStream = TerminalConsolePrintStream(terminalWriterOutputStream)
    private val terminalOutputService = TerminalOutputService()

    override fun initialize() {
        apply {
            connect("_emulator_mem")

            /*
             * Prevent auto caret movement when user pressed '←', '→', '↑', '↓', 'Home', 'PgUp', 'PgDn', instead of setting the caret manually
             */
            addEventFilter(KeyEvent.ANY) { event: KeyEvent ->
                var input = ""
                when (event.code) {
                    KeyCode.LEFT -> {
                        input = CharUtil.LEFT_ARROW.toString()
                        event.consume()
                    }
                    KeyCode.RIGHT -> {
                        input = CharUtil.RIGHT_ARROW.toString()
                        event.consume()
                    }
                    KeyCode.UP -> {
                        input = CharUtil.UP_ARROW.toString()
                        event.consume()
                    }
                    KeyCode.DOWN -> {
                        input = CharUtil.DOWN_ARROW.toString()
                        event.consume()
                    }
                    KeyCode.END, KeyCode.PAGE_DOWN, KeyCode.PAGE_UP, KeyCode.HOME -> event.consume()
                    else -> {}
                }
                if (input.isNotEmpty()) {
                    TerminalEmulator.terminalReaderInputStream.write(input.toByteArray(StandardCharsets.UTF_8))
                    TerminalEmulator.terminalReaderInputStream.flush()
                }
            }

            onKeyTyped = EventHandler { event: KeyEvent ->
                if (event.isShortcutDown || event.isControlDown || event.isAltDown || event.isMetaDown) {
                    return@EventHandler
                }
                try {
                    terminalReaderInputStream.write(event.character.toByteArray(StandardCharsets.UTF_8))
                    terminalReaderInputStream.flush()
                } catch (e: IOException) {
                    error("Write to reader failed, msg = ${e.message}")
                }
            }

            addEventHandler(KeyEvent.KEY_PRESSED) { }
            addEventHandler(KeyEvent.KEY_RELEASED) { }
            addNativeEventListener {key, type, evt ->
                if (WindowSizeAdjuster.onMoved && updateOnce) {
                    println("> key: $key, type: $type, evt_msg: $evt")
                    updateNativeImage()
                    updateOnce = false
                }
            }

            terminalOutputService.start()

            onMouseClicked = EventHandler { requestFocus() }

            focusedProperty()
                .addListener { _: ObservableValue<out Boolean>?, _: Boolean?, nv: Boolean ->
                    if (nv) {
                        activeTerminal()
                    }
                    NativeBinding.requestFocus(key, nv, System.nanoTime())
                }
        }
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run {
            prefWidthProperty().bind(major.widthProperty().multiply(widthRatio))
        }
        heightRatio?.run {
            prefHeightProperty().bind(major.heightProperty().subtract(TopMenuPanel.fixedHeight + TopSessionTabBar.fixedHeight + BottomStatusBar.fixedHeight).multiply(heightRatio))
        }
    }

    override fun styleClasses(): Array<String> {
        return arrayOf()
    }

    override fun actionAfterShow() {}

    private fun activeTerminal() {
        TerminalEmulator.currentActiveId = id()
        Printer.setPrinter(terminalPrintStream)
    }

    private inner class TerminalOutputService : Loggable {
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

    companion object {
        @Volatile
        var currentActiveId: Long = 0

        /**
         * DesktopTerminalPanel has only one MetadataReaderInputStream:
         * Get user's input data.
         */
        @JvmField
        val terminalReaderInputStream = MetadataReaderInputStream()
    }
}