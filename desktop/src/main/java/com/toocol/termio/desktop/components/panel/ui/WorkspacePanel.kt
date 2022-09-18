package com.toocol.termio.desktop.components.panel.ui

import com.toocol.termio.core.cache.ShellCache
import com.toocol.termio.desktop.components.homepage.ui.Homepage
import com.toocol.termio.desktop.components.sidebar.ui.BottomStatusBar
import com.toocol.termio.desktop.components.sidebar.ui.TopSessionTabBar
import com.toocol.termio.desktop.components.terminal.ui.TerminalEmulatorFactory
import com.toocol.termio.platform.component.Component
import com.toocol.termio.platform.component.ComponentsParser
import com.toocol.termio.platform.component.RegisterComponent
import com.toocol.termio.platform.ui.TStackPane
import javafx.application.Platform
import javafx.scene.layout.Pane
import java.util.*
import java.util.concurrent.CountDownLatch

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
    private val terminalFactory: TerminalEmulatorFactory.Instance = TerminalEmulatorFactory.Instance
    private val terminalIds: MutableSet<Int> = TreeSet()
    private val shellCache: ShellCache.Instance = ShellCache.Instance

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

        children.add(parser.getAsNode(Homepage::class.java))
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        val panel = this
        widthRatio?.run {
            prefWidthProperty().bind(major.widthProperty().multiply(widthRatio))
            panel.widthRatio = widthRatio
        }
        heightRatio?.run {
            prefHeightProperty().bind(major.heightProperty().subtract(TopMenuPanel.fixedHeight + TopSessionTabBar.fixedHeight + BottomStatusBar.fixedHeight).multiply(heightRatio))
            panel.heightRatio = heightRatio
        }

        parser.getAsComponent(Homepage::class.java)?.sizePropertyBind(major, widthRatio, if (heightRatio == null) null else heightRatio * 0.985)
        terminalFactory.getAllTerminals()
            .asSequence()
            .forEach { it.sizePropertyBind(major, widthRatio, if (heightRatio == null) null else heightRatio * 0.985) }
    }

    override fun actionAfterShow() {}

    fun createTerminal(sessionId: Long) {
        val latch = CountDownLatch(1)
        Platform.runLater {
            hideHomepage()
            val terminal = terminalFactory.create(assignTerminalId(), sessionId)
            terminal.initialize()
            terminal.sizePropertyBind(findComponent(MajorPanel::class.java, 1), widthRatio, heightRatio * 0.985)
//            shellCache.getShell(sessionId)!!.registerConsole(TerminalConsole(terminal.getTerminalTextAre()))
            children.add(terminal)
            terminal.activeTerminal()
            terminal.requestFocus()
            latch.countDown()
        }
        latch.await()
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