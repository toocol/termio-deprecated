package com.toocol.termio.desktop.components.executor.ui

import com.toocol.termio.core.term.TermAddress
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.desktop.api.term.handlers.DynamicEchoHandler
import com.toocol.termio.desktop.components.panel.ui.MajorPanel
import com.toocol.termio.desktop.components.panel.ui.WorkspacePanel
import com.toocol.termio.desktop.components.terminal.ui.TerminalConsole
import com.toocol.termio.platform.console.MetadataPrinterOutputStream
import com.toocol.termio.platform.console.MetadataReaderInputStream
import com.toocol.termio.platform.console.TerminalConsolePrintStream
import com.toocol.termio.platform.ui.TScene
import com.toocol.termio.platform.ui.TVBox
import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.CharUtil
import com.toocol.termio.utilities.utils.StrUtil
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Pane
import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/12 0:42
 * @version: 0.0.1
 */
class CommandExecutor(id: Long) : TVBox(id), Loggable {
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
            Term.registerConsole(TerminalConsole(commandExecutorResultTextArea))

            focusedProperty().addListener { _: ObservableValue<out Boolean>?, _: Boolean?, newVal: Boolean ->
                if (newVal) {
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
            }

            val ctrlI: KeyCombination = KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN)
            scene.accelerators[ctrlI] = Runnable {
                if (!commandExecutorInput.isFocused && isVisible) {
                    commandExecutorInput.requestFocus()
                } else {
                    val ratio = if (isVisible) {
                        hide()
                        1.0
                    } else {
                        show()
                        commandExecutorInput.requestFocus()
                        0.8
                    }
                    val majorPanel = findComponent(MajorPanel::class.java, 1)
                    findComponent(WorkspacePanel::class.java, 1).sizePropertyBind(majorPanel, null, ratio)
                }
            }

            children.addAll(commandExecutorInput, commandExecutorResultScrollPane)
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
            onMouseClicked = EventHandler { commandExecutorInput.requestFocus() }
        }
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty().multiply(widthRatio)) }
        heightRatio?.run { prefHeightProperty().bind(major.heightProperty().multiply(heightRatio)) }

        commandExecutorInput.sizePropertyBind(major, widthRatio, if (heightRatio == null) null else heightRatio * 0.15)
        commandExecutorResultTextArea.sizePropertyBind(major, widthRatio, if (heightRatio == null) null else heightRatio * 0.85)
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
                            commandExecutorResultTextArea.append(text, commandExecutorPrintStream)
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
        val commandExecutorPrintStream = TerminalConsolePrintStream(commandExecutorPrinterOutputStream)
    }
}