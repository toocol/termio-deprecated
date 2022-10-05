package com.toocol.termio.desktop.components.panel.ui

import com.toocol.termio.desktop.components.homepage.ui.Homepage
import com.toocol.termio.desktop.components.sidebar.ui.BottomStatusBar
import com.toocol.termio.desktop.components.terminal.ui.NativeTerminalEmulator
import com.toocol.termio.platform.component.Component
import com.toocol.termio.platform.component.ComponentsParser
import com.toocol.termio.platform.component.RegisterComponent
import com.toocol.termio.platform.ui.TStackPane
import javafx.scene.layout.Pane
import kotlinx.coroutines.launch

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/12 0:39
 * @version: 0.0.1
 */
@RegisterComponent(value = [
    Component(clazz = Homepage::class, id = 1, initialVisible = true),
])
class WorkspacePanel(id: Long) : TStackPane(id) {

    private val parser: ComponentsParser = ComponentsParser()

    private var widthRatio: Double = 1.0
    private var heightRatio: Double = 1.0

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "workspace-panel"
        )
    }

    override fun initialize() {
        styled()

        parser.initializeAll()

        children.addAll(parser.getAsNode(Homepage::class.java), NativeTerminalEmulator)
        NativeTerminalEmulator.hide()
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        val panel = this
        widthRatio?.run {
            prefWidthProperty().bind(major.widthProperty().multiply(widthRatio))
            panel.widthRatio = widthRatio
        }
        heightRatio?.run {
            prefHeightProperty().bind(major.heightProperty().subtract(TopMenuPanel.fixedHeight + BottomStatusBar.fixedHeight).multiply(heightRatio))
            panel.heightRatio = heightRatio
        }

        parser.getAsComponent(Homepage::class.java)?.sizePropertyBind(major, widthRatio, heightRatio)
        NativeTerminalEmulator.sizePropertyBind(major, widthRatio, heightRatio)
    }

    override fun actionAfterShow() {}

    fun createSshSession(sessionId: Long, host: String, user: String, password: String) {
        launch {
            hideHomepage()
            val terminal = NativeTerminalEmulator
            terminal.initialize()
            terminal.sizePropertyBind(findComponent(MajorPanel::class.java, 1), widthRatio, heightRatio)
            terminal.createSshSession(sessionId, host, user, password)
            terminal.activeTerminal()
            terminal.requestFocus()
            terminal.show()
            System.gc()
        }
    }

    fun invokeTerminal(sessionId: Long) {

    }

    private fun assignTerminalId(): Long {
        return 0
    }

    private fun showHomepage() {
        val homepage = parser.getAsComponent(Homepage::class.java)
        homepage?.show()
    }

    private fun hideHomepage() {
        val homepage = parser.getAsComponent(Homepage::class.java)
        homepage?.hide()
    }

    init {
        parser.parse(WorkspacePanel::class.java)
    }
}