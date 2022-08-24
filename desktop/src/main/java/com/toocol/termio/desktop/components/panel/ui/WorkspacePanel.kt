package com.toocol.termio.desktop.components.panel.ui

import com.toocol.termio.core.cache.ShellCache
import com.toocol.termio.desktop.components.homepage.ui.Homepage
import com.toocol.termio.desktop.components.terminal.ui.DesktopConsole
import com.toocol.termio.desktop.components.terminal.ui.DesktopTerminalFactory
import com.toocol.termio.platform.component.Component
import com.toocol.termio.platform.component.ComponentsParser
import com.toocol.termio.platform.component.RegisterComponent
import com.toocol.termio.platform.ui.TStackPane
import javafx.application.Platform
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
    private val terminalFactory: DesktopTerminalFactory.Instance = DesktopTerminalFactory.Instance
    private val terminalIds: MutableSet<Int> = TreeSet()
    private val shellCache: ShellCache.Instance = ShellCache.Instance

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "workspace-panel"
        )
    }

    override fun initialize() {
        styled()
        val majorPanel = findComponent(MajorPanel::class.java, 1)
        prefWidthProperty().bind(majorPanel.widthProperty().multiply(0.85))
        prefHeightProperty().bind(majorPanel.heightProperty().multiply(0.8))

        parser.parse(WorkspacePanel::class.java)
        parser.initializeAll()

        children.add(parser.getAsNode(Homepage::class.java))
    }

    override fun actionAfterShow() {}

    fun createTerminal(sessionId: Long) {
        val latch = CountDownLatch(1)
        Platform.runLater {
            hideHomepage()
            val terminal = terminalFactory.create(assignTerminalId(), sessionId)
            terminal.initialize()
            shellCache.getShell(sessionId)!!.registerConsole(DesktopConsole(terminal.getConsoleTextAre()))
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
}