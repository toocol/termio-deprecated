package com.toocol.termio.desktop.ui.executor

import com.toocol.termio.core.term.TermAddress
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.desktop.api.term.handlers.DynamicEchoHandler
import com.toocol.termio.desktop.ui.panel.WorkspacePanel
import com.toocol.termio.desktop.ui.terminal.DesktopConsole
import com.toocol.termio.desktop.ui.terminal.DesktopTerminal
import com.toocol.termio.desktop.ui.terminal.TerminalConsoleTextArea
import com.toocol.termio.platform.console.MetadataPrinterOutputStream
import com.toocol.termio.platform.console.MetadataReaderInputStream
import com.toocol.termio.platform.ui.TBorderPane
import com.toocol.termio.platform.ui.TScene
import com.toocol.termio.utilities.ansi.Printer.setPrinter
import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.CharUtil
import com.toocol.termio.utilities.utils.StrUtil
import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import java.io.IOException
import java.io.PrintStream
import java.nio.charset.StandardCharsets

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/12 0:42
 * @version: 0.0.1
 */
class CommandExecutor(id: Long) : TBorderPane(id), Loggable {
    private val executorOutputService = ExecutorOutputService()
    private val commandExecutorInput: CommandExecutorInput
    private val commandExecutorResultTextArea: CommandExecutorResultTextArea
    private val commandExecutorResultScrollPane: CommandExecutorResultScrollPane

    private var commandOut = false

    init {
        commandExecutorInput = CommandExecutorInput(id)
        commandExecutorResultTextArea = CommandExecutorResultTextArea(id)
        commandExecutorResultScrollPane = CommandExecutorResultScrollPane(id, commandExecutorResultTextArea)
    }

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "command-executor-panel"
        )
    }

    override fun initialize() {
        apply {
            styled()
            Term.registerConsole(DesktopConsole(commandExecutorResultTextArea))
            val workspacePanel = findComponent(WorkspacePanel::class.java, id)
            prefWidthProperty().bind(workspacePanel.prefWidthProperty().multiply(1))
            prefHeightProperty().bind(workspacePanel.prefHeightProperty().multiply(0.2))

            focusedProperty().addListener { _: ObservableValue<out Boolean>?, _: Boolean?, newVal: Boolean ->
                if (newVal) {
                    setPrinter(commandExecutorPrintStream)
                    println("Executor get focus")
                } else {
                    println("Executor lose focus")
                }
            }

            val scene = findComponent(TScene::class.java, 1)
            val ctrlU: KeyCombination = KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN)
            scene.accelerators[ctrlU] = Runnable {
                commandExecutorInput.clear()
                commandExecutorResultTextArea.clear()
                commandExecutorResultTextArea.cursorTest()
            }

            val ctrlAltP: KeyCombination =
                KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN)
            scene.accelerators[ctrlAltP] = Runnable {
                if (!commandExecutorInput.isFocused && isVisible) {
                    commandExecutorInput.requestFocus()
                } else {
                    val ratio = if (isVisible) {
                        hide()
                        findComponent(TerminalConsoleTextArea::class.java, 1).requestFocus()
                        1.0
                    } else {
                        show()
                        commandExecutorInput.requestFocus()
                        0.8
                    }
                    findComponent(DesktopTerminal::class.java, 1).prefHeightProperty()
                        .bind(workspacePanel.prefHeightProperty().multiply(ratio))
                }
            }
        }

        executorOutputService.apply { start() }
        commandExecutorResultTextArea.apply {
            initialize()
            onMouseClicked = EventHandler { commandExecutorInput.requestFocus() }
        }
        commandExecutorResultScrollPane.apply { initialize() }
        commandExecutorInput.apply {
            initialize()
            addEventFilter(KeyEvent.KEY_TYPED) { event: KeyEvent ->
                if (StrUtil.isNewLine(event.character)) {
                    try {
                        commandOut = true
                        val command = commandExecutorInput.text + StrUtil.LF
                        executorReaderInputStream.write(command.toByteArray(StandardCharsets.UTF_8))
                        executorReaderInputStream.flush()
                        commandExecutorInput.clear()
                    } catch (e: IOException) {
                        error("Write to reader failed, msg = ${e.message}")
                    }
                    event.consume()
                } else if (text.length >= 100) {
                    event.consume()
                }
            }
            addEventFilter(KeyEvent.KEY_PRESSED) { event: KeyEvent ->
                if (event.code == KeyCode.UP || event.code == KeyCode.DOWN) {
                    executorReaderInputStream.write((if (event.code == KeyCode.UP) CharUtil.UP_ARROW.toString() else CharUtil.DOWN_ARROW.toString()).toByteArray(
                        StandardCharsets.UTF_8))
                    executorReaderInputStream.flush()
                    event.consume()
                }
            }
            textProperty().addListener { _, _, newVal ->
                if (commandOut) {
                    DynamicEchoHandler.lastInput = StrUtil.EMPTY
                    commandOut = false
                } else {
                    eventBus().send(TermAddress.TERMINAL_ECHO.address(), newVal)
                }
            }
            focusedProperty()
                .addListener { _: ObservableValue<out Boolean>?, _: Boolean?, newVal: Boolean ->
                    if (newVal) {
                        setPrinter(commandExecutorPrintStream)
                        println("Executor input get focus")
                    } else {
                        println("Executor input lose focus")
                    }
                }
            onMouseClicked = EventHandler { commandExecutorInput.requestFocus() }
        }
    }

    override fun actionAfterShow() {
        commandExecutorInput.requestFocus()
    }

    private inner class ExecutorOutputService : Loggable {
        fun start() {
            val thread = Thread({
                while (true) {
                    try {
                        if (commandExecutorPrinterOutputStream.available() > 0) {
                            val text = commandExecutorPrinterOutputStream.read()
                            Platform.runLater { commandExecutorResultTextArea.append(text) }
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
         * CommandExecutorPanel has only one MetadataReaderInputStream:
         * Get user's input data.
         */
        @JvmField
        val executorReaderInputStream = MetadataReaderInputStream()

        /**
         * CommandExecutorPanel has only one MetadataPrinterOutputStream and PrintStream correspondent:
         * Feedback data.
         */
        val commandExecutorPrinterOutputStream = MetadataPrinterOutputStream()

        @JvmField
        val commandExecutorPrintStream = PrintStream(commandExecutorPrinterOutputStream)
    }
}