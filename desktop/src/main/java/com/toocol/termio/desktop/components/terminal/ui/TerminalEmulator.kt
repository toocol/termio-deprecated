package com.toocol.termio.desktop.components.terminal.ui

import com.toocol.termio.platform.console.MetadataPrinterOutputStream
import com.toocol.termio.platform.console.MetadataReaderInputStream
import com.toocol.termio.platform.console.TerminalConsolePrintStream
import com.toocol.termio.platform.ui.TAnchorPane
import com.toocol.termio.utilities.ansi.Printer.setPrinter
import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.StrUtil
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.scene.input.InputMethodEvent
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
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

            focusedProperty().addListener {_, _, newVal ->
                if (newVal) {
                    terminalEmulatorTextArea.requestFocus()
                }
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
                when (event.code) {
                    KeyCode.LEFT -> {
                        event.consume()
                        if (cursor.inlinePosition > currentLineStartInParargraph) {
                            cursor.moveLeft(1)
                        }
                    }
                    KeyCode.RIGHT -> {
                        event.consume()
                        val index = paragraphs.size - 1
                        if (cursor.inlinePosition < getParagraphLength(index)) {
                            cursor.moveRight(1)
                        }
                    }
                    KeyCode.END -> {
                        cursor.setTo(length)
                    }
                    KeyCode.UP, KeyCode.DOWN, KeyCode.PAGE_DOWN, KeyCode.PAGE_UP, KeyCode.HOME -> event.consume()
                    else -> {}
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
        heightRatio?.run { prefHeightProperty().bind(major.heightProperty().multiply(heightRatio)) }

        terminalEmulatorTextArea.sizePropertyBind(major, widthRatio, heightRatio)
    }

    override fun actionAfterShow() {}

    fun activeTerminal() {
        currentActiveId = id()
        setPrinter(terminalPrintStream)
    }

    fun getConsoleTextAre(): TerminalEmulatorTextArea {
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