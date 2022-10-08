package com.toocol.termio.desktop.components.executor.ui

import com.toocol.termio.core.term.api.DynamicEchoApi
import com.toocol.termio.core.term.api.ExecuteCommandApi
import com.toocol.termio.platform.console.MetadataPrinterOutputStream
import com.toocol.termio.platform.console.MetadataReaderInputStream
import com.toocol.termio.platform.console.TerminalConsolePrintStream
import com.toocol.termio.platform.ui.TVBox
import com.toocol.termio.platform.window.StageHolder
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/12 0:42
 * @version: 0.0.1
 */
class CommandExecutor : TVBox(), Loggable {
    private val executorOutputService = ExecutorOutputService()
    private val commandExecutorInput: CommandExecutorInput = CommandExecutorInput()
    private val commandExecutorResultTextArea: CommandExecutorResultTextArea = CommandExecutorResultTextArea()
    private val commandExecutorResultScrollPane: CommandExecutorResultScrollPane = CommandExecutorResultScrollPane(commandExecutorResultTextArea)

    private var commandOut = false

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "command-executor-panel"
        )
    }

    override fun initialize() {
        apply {
            styled()

            focusedProperty().addListener { _: ObservableValue<out Boolean>?, _: Boolean?, newVal: Boolean ->
                if (newVal) {
                    println("Executor get focus")
                } else {
                    println("Executor lose focus")
                }
            }

            val scene = StageHolder.scene!!
            val ctrlU: KeyCombination = KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN)
            scene.accelerators[ctrlU] = Runnable {
                commandExecutorInput.clear()
                commandExecutorResultTextArea.clear()
                commandExecutorResultScrollPane.hide()
            }

            val ctrlI: KeyCombination = KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN)
            scene.accelerators[ctrlI] = Runnable {
                if (!commandExecutorInput.isFocus() && isVisible) {
                    commandExecutorInput.requestFocus()
                } else {
                    if (isVisible) {
                        this.hide()
                    } else {
                        this.show()
                        commandExecutorInput.requestFocus()
                    }
                }
            }

            spacing = 6.0
            children.addAll(commandExecutorResultScrollPane, commandExecutorInput)
        }

        executorOutputService.apply { start() }

        commandExecutorResultTextArea.apply {
            initialize()
            onMouseClicked = EventHandler { commandExecutorInput.requestFocus() }
        }

        commandExecutorResultScrollPane.apply {
            initialize()
            hide()
        }

        commandExecutorInput.apply {
            initialize()
            addEventFilter(KeyEvent.KEY_TYPED) { event: KeyEvent ->
                if (StrUtil.isNewLine(event.character)) {
                    try {
                        commandOut = true
                        val command = commandExecutorInput.text()
                        launch {
                            api(ExecuteCommandApi, Dispatchers.Default) {
                                executeCommand(command)
                            }
                        }
                        commandExecutorInput.clear()
                    } catch (e: IOException) {
                        error("Write to reader failed, msg = ${e.message}")
                    }
                    event.consume()
                } else if (text().length >= 100) {
                    event.consume()
                }
            }
            addEventFilter(KeyEvent.KEY_PRESSED) { event: KeyEvent ->
                if (event.code == KeyCode.UP || event.code == KeyCode.DOWN) {
                    executorReaderInputStream.write(
                        (if (event.code == KeyCode.UP) CharUtil.UP_ARROW.toString() else CharUtil.DOWN_ARROW.toString()).toByteArray(
                            StandardCharsets.UTF_8
                        )
                    )
                    executorReaderInputStream.flush()
                    event.consume()
                }
            }
            textProperty().addListener { _, _, newVal ->
                if (commandOut) {
                    DynamicEchoApi.lastInput = StrUtil.EMPTY
                    commandOut = false
                } else {
                    launch(Dispatchers.Default) {
                        DynamicEchoApi.dynamicEcho(newVal)
                    }
                }
            }
            onMouseClicked = EventHandler { commandExecutorInput.requestFocus() }
        }
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty().multiply(widthRatio)) }

        commandExecutorInput.sizePropertyBind(major, widthRatio, null)
        commandExecutorResultTextArea.sizePropertyBind(major, widthRatio, heightRatio)
    }

    override fun actionAfterShow() {
        commandExecutorInput.requestFocus()
    }

    private inner class ExecutorOutputService : Loggable, CoroutineScope by MainScope() {
        fun start() {
            val thread = Thread({
                while (true) {
                    try {
                        if (commandExecutorPrinterOutputStream.available() > 0) {
                            val text = commandExecutorPrinterOutputStream.read()
                            commandExecutorResultTextArea.append(text, commandExecutorPrintStream)
                            if (!commandExecutorResultScrollPane.isVisible) {
                                commandExecutorResultScrollPane.show()
                            }
                        }
                        Thread.sleep(1)
                    } catch (e: Exception) {
                        warn("ExecutorOutputService catch exception, e = ${e.javaClass.name}, msg = ${e.message}")
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