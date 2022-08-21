package com.toocol.termio.desktop.ui.terminal

import com.toocol.termio.desktop.ui.panel.WorkspacePanel
import com.toocol.termio.platform.component.IActiveAble
import com.toocol.termio.platform.console.MetadataPrinterOutputStream
import com.toocol.termio.platform.console.MetadataReaderInputStream
import com.toocol.termio.platform.ui.TAnchorPane
import com.toocol.termio.platform.ui.TScene
import com.toocol.termio.utilities.ansi.Printer.setPrinter
import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.StrUtil
import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.scene.input.*
import java.io.IOException
import java.io.PrintStream
import java.nio.charset.StandardCharsets

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/4 11:06
 */
class DesktopTerminalPanel(id: Long) : TAnchorPane(id), IActiveAble, Loggable {
    /**
     * Each DesktopTerminalPanel or CommandExecutorPanel has one onw MetadataPrinterOutputStream and PrintStream correspondent:
     * Feedback data.
     */
    private val terminalWriterOutputStream = MetadataPrinterOutputStream()
    private val terminalPrintStream = PrintStream(terminalWriterOutputStream)
    private val terminalOutputService = TerminalOutputService()
    private val terminalScrollPane: TerminalScrollPane
    private val terminalConsoleTextArea: TerminalConsoleTextArea

    init {
        terminalConsoleTextArea = TerminalConsoleTextArea(id)
        terminalScrollPane = TerminalScrollPane(id, terminalConsoleTextArea)
    }

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "desktop-terminal-panel"
        )
    }

    override fun initialize() {
        apply {
            styled()
            val scene = findComponent(TScene::class.java, 1)
            val workspacePanel = findComponent(WorkspacePanel::class.java, 1)
            workspacePanel.center = findComponent(DesktopTerminalPanel::class.java, id)

            maxHeightProperty().bind(workspacePanel.prefHeightProperty())
            maxWidthProperty().bind(workspacePanel.prefWidthProperty())
            prefHeightProperty().bind(workspacePanel.prefHeightProperty().multiply(0.8))
            prefWidthProperty().bind(workspacePanel.prefWidthProperty())
            children.add(terminalScrollPane)

            val ctrlT: KeyCombination = KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN)
            scene.accelerators[ctrlT] = Runnable { terminalConsoleTextArea.clear() }
        }

        terminalOutputService.apply { start() }

        terminalScrollPane.apply { initialize() }

        terminalConsoleTextArea.apply {
            initialize()

            onInputMethodTextChanged = EventHandler { event: InputMethodEvent ->
                if (StrUtil.isEmpty(event.committed)) {
                    return@EventHandler
                }
                try {
                    terminalReaderInputStream.write(event.committed.toByteArray(StandardCharsets.UTF_8))
                    terminalReaderInputStream.flush()
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
            onMouseClicked = EventHandler { terminalConsoleTextArea.requestFocus() }

            focusedProperty()
                .addListener { _: ObservableValue<out Boolean>?, _: Boolean?, newVal: Boolean ->
                    if (newVal) {
                        activeTerminal()
                        println("Terminal get focus")
                    } else {
                        println("Terminal lose focus")
                    }
                }
        }
    }

    override fun actionAfterShow() {}

    override fun active() {}

    fun activeTerminal() {
        currentActiveId = id()
        setPrinter(terminalPrintStream)
    }

    private inner class TerminalOutputService : Loggable {
        fun start() {
            val thread = Thread({
                while (true) {
                    try {
                        if (terminalWriterOutputStream.available() > 0) {
                            val text = terminalWriterOutputStream.read()
                            Platform.runLater { terminalConsoleTextArea.append(text) }
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