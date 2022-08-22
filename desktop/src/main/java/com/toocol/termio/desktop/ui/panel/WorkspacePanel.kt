package com.toocol.termio.desktop.ui.panel

import com.toocol.termio.desktop.ui.executor.CommandExecutor
import com.toocol.termio.desktop.ui.homepage.Homepage
import com.toocol.termio.desktop.ui.terminal.DesktopTerminalFactory
import com.toocol.termio.platform.component.Component
import com.toocol.termio.platform.component.ComponentsParser
import com.toocol.termio.platform.component.RegisterComponent
import com.toocol.termio.platform.ui.TBorderPane

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/12 0:39
 * @version: 0.0.1
 */
@RegisterComponent(value = [
    Component(clazz = Homepage::class, id = 1, initialVisible = true),
    Component(clazz = CommandExecutor::class, id = 1, initialVisible = true)
])
class WorkspacePanel(id: Long) : TBorderPane(id) {

    private val parser: ComponentsParser = ComponentsParser()
    private val terminalFactory: DesktopTerminalFactory = DesktopTerminalFactory()

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "workspace-panel"
        )
    }

    override fun initialize() {
        parser.parse(WorkspacePanel::class.java)
        parser.initializeAll()

        styled()

        val centralPanel = findComponent(CentralPanel::class.java, 1)
        maxHeightProperty().bind(centralPanel.heightProperty())
        maxWidthProperty().bind(centralPanel.widthProperty())
        prefHeightProperty().bind(centralPanel.heightProperty())
        prefWidthProperty().bind(centralPanel.widthProperty())


        top = parser.get(Homepage::class.java)
        bottom = parser.get(CommandExecutor::class.java)
    }

    override fun actionAfterShow() {}
}