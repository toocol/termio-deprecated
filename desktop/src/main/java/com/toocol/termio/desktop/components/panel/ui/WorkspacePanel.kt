package com.toocol.termio.desktop.components.panel.ui

import com.toocol.termio.desktop.components.homepage.ui.Homepage
import com.toocol.termio.desktop.components.terminal.ui.DesktopTerminalFactory
import com.toocol.termio.platform.component.Component
import com.toocol.termio.platform.component.ComponentsParser
import com.toocol.termio.platform.component.RegisterComponent
import com.toocol.termio.platform.ui.TStackPane

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
    private val terminalFactory: DesktopTerminalFactory = DesktopTerminalFactory()

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "workspace-panel"
        )
    }

    override fun initialize() {
        styled()
        parser.parse(WorkspacePanel::class.java)
        parser.initializeAll()

        val centerPanel = findComponent(CenterPanel::class.java, 1)
        maxHeightProperty().bind(centerPanel.heightProperty())
        maxWidthProperty().bind(centerPanel.widthProperty())
        prefHeightProperty().bind(centerPanel.heightProperty())
        prefWidthProperty().bind(centerPanel.widthProperty())

        children.add(parser.get(Homepage::class.java))
    }

    override fun actionAfterShow() {}
}