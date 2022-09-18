package com.toocol.termio.desktop.components.terminal.ui

import com.toocol.termio.desktop.components.panel.ui.TopMenuPanel
import com.toocol.termio.desktop.components.sidebar.ui.BottomStatusBar
import com.toocol.termio.desktop.components.sidebar.ui.TopSessionTabBar
import com.toocol.termio.platform.nativefx.NativeBinding
import com.toocol.termio.platform.nativefx.NativeNode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Pane

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/18 18:35
 * @version: 0.0.1
 */
class NativeTerminalEmulator(id: Long, sessionId: Long) : NativeNode(id, true, true) {
    private val memName = "_emulator_mem"

    override fun initialize() {
        apply {
            connect(memName)

            addEventHandler(KeyEvent.KEY_PRESSED) { ev: KeyEvent ->
                // System.out.println("KEY: pressed " + ev.getText() + " : " + ev.getCode());
                val timestamp = System.nanoTime()
                NativeBinding.fireKeyPressedEvent(key, ev.text, ev.code.code,  /*modifiers*/
                    0,
                    timestamp
                )
            }
            addEventHandler(KeyEvent.KEY_RELEASED) { ev: KeyEvent ->
                // System.out.println("KEY: released " + ev.getText() + " : " + ev.getCode());
                val timestamp = System.nanoTime()
                NativeBinding.fireKeyReleasedEvent(key, ev.text, ev.code.code,  /*modifiers*/
                    0,
                    timestamp
                )
            }
            addEventHandler(KeyEvent.KEY_TYPED) { ev: KeyEvent ->
                // System.out.println("KEY: typed    " + ev.getText() + " : " + ev.getCode());
                val timestamp = System.nanoTime()
                NativeBinding.fireKeyTypedEvent(key, ev.text, ev.code.code,  /*modifiers*/
                    0,
                    timestamp
                )
            }
        }
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty().multiply(widthRatio)) }
        heightRatio?.run { prefHeightProperty().bind(major.heightProperty().subtract(TopMenuPanel.fixedHeight + TopSessionTabBar.fixedHeight + BottomStatusBar.fixedHeight).multiply(heightRatio)) }
    }

    override fun styleClasses(): Array<String> {
        return arrayOf()
    }

    override fun actionAfterShow() {}

    fun activeTerminal() {
        TerminalEmulator.currentActiveId = id()
//        Printer.setPrinter(terminalPrintStream)
    }
}