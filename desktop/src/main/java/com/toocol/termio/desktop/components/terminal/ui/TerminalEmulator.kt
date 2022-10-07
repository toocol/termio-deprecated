package com.toocol.termio.desktop.components.terminal.ui

import com.toocol.termio.desktop.components.panel.ui.TopMenuPanel
import com.toocol.termio.desktop.components.sidebar.ui.BottomStatusBar
import com.toocol.termio.platform.console.MetadataPrinterOutputStream
import com.toocol.termio.platform.console.MetadataReaderInputStream
import com.toocol.termio.platform.console.TerminalConsolePrintStream
import com.toocol.termio.platform.ui.TAnchorPane
import com.toocol.termio.utilities.ansi.Printer.setPrinter
import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.CharUtil
import com.toocol.termio.utilities.utils.StrUtil
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.scene.input.*
import javafx.scene.layout.Pane
import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/4 11:06
 */
class TerminalEmulator(id: Long, sessionId: Long) : TAnchorPane(id), Loggable {
    /**
     * Each DesktopTerminalPanel or CommandExecutorPanel has one onw MetadataPrinterOutputStream and PrintStream correspondent:
     * Feedback data.
     */
    private val terminalWriterOutputStream = MetadataPrinterOutputStream()
    private val terminalPrintStream = TerminalConsolePrintStream(terminalWriterOutputStream)
    private val terminalOutputService = TerminalOutputService()
    private val terminalScrollPane: TerminalScrollPane
    private val terminalEmulatorTextArea: TerminalEmulatorTextArea

    init {
        terminalEmulatorTextArea = TerminalEmulatorTextArea(id)
        terminalScrollPane = TerminalScrollPane(id, terminalEmulatorTextArea)
    }

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "desktop-terminal-panel",
        )
    }

    override fun initialize() {
        apply {
            styled()

            children.add(terminalScrollPane)

            setOnMouseClicked { terminalEmulatorTextArea.requestFocus() }

            focusedProperty().addListener { _, _, newVal ->
                if (newVal) {
                    terminalEmulatorTextArea.requestFocus()
                }
            }

            val ctrlU: KeyCombination = KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN)
            scene.accelerators[ctrlU] = Runnable {
                println("line: ${terminalEmulatorTextArea.getCursorPos()[0]}")
            }
        }

        terminalOutputService.apply { start() }

        terminalScrollPane.apply { initialize() }

        terminalEmulatorTextArea.apply {
            initialize()

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
                    terminalReaderInputStream.write(input.toByteArray(StandardCharsets.UTF_8))
                    terminalReaderInputStream.flush()
                }
            }

            onInputMethodTextChanged = EventHandler { event: InputMethodEvent ->
                if (StrUtil.isEmpty(event.committed)) {
                    return@EventHandler
                }
                try {
                    if (StrUtil.isChineseSequence(event.committed)) {
                        terminalReaderInputStream.write(event.committed.toByteArray(StandardCharsets.UTF_8))
                        terminalReaderInputStream.flush()
                    }
                } catch (e: IOException) {
                    error("Write to reader failed, msg = ${e.message}")
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
            onMouseClicked = EventHandler { terminalEmulatorTextArea.requestFocus() }

            focusedProperty()
                .addListener { _: ObservableValue<out Boolean>?, _: Boolean?, newVal: Boolean ->
                    if (newVal) {
                        activeTerminal()
                    }
                }
        }
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty().multiply(widthRatio)) }
        heightRatio?.run {
            prefHeightProperty().bind(major.heightProperty()
                .subtract(TopMenuPanel.fixedHeight + BottomStatusBar.fixedHeight).multiply(heightRatio))
        }

        terminalEmulatorTextArea.sizePropertyBind(major, widthRatio, heightRatio)
    }

    override fun actionAfterShow() {}

    private fun activeTerminal() {
        setPrinter(terminalPrintStream)
    }

    fun getTerminalTextAre(): TerminalEmulatorTextArea {
        return terminalEmulatorTextArea
    }

    private inner class TerminalOutputService : Loggable {
        fun start() {
            val thread = Thread({
                while (true) {
                    try {
                        if (terminalWriterOutputStream.available() > 0) {
                            val text = terminalWriterOutputStream.read()
                            terminalEmulatorTextArea.append(text, terminalPrintStream)
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
        /**
         * DesktopTerminalPanel has only one MetadataReaderInputStream:
         * Get user's input data.
         */
        @JvmField
        val terminalReaderInputStream = MetadataReaderInputStream()
    }
}