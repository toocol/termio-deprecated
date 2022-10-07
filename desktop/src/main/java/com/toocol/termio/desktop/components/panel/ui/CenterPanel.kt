package com.toocol.termio.desktop.components.panel.ui

import com.toocol.termio.desktop.components.executor.ui.CommandExecutor
import com.toocol.termio.platform.component.Component
import com.toocol.termio.platform.component.ComponentsParser
import com.toocol.termio.platform.component.RegisterComponent
import com.toocol.termio.platform.ui.TVBox
import javafx.scene.layout.Pane

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 12:46
 * @version: 0.0.1
 */
@RegisterComponent(value = [
    Component(clazz = WorkspacePanel::class, id = 1, initialVisible = true),
    Component(clazz = CommandExecutor::class, id = 1, initialVisible = true),
])
class CenterPanel(id: Long) : TVBox(id) {

    private val parser: ComponentsParser = ComponentsParser()

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "center-panel"
        )
    }

    override fun initialize() {
        styled()

        parser.initializeAll()

        val workspacePanel = parser.getAsNode(WorkspacePanel::class.java)
        val commandExecutor = parser.getAsNode(CommandExecutor::class.java)
        children.addAll(
            workspacePanel,
            commandExecutor
        )
        workspacePanel?.viewOrder = 10.0
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty().multiply(widthRatio)) }
        heightRatio?.run { prefHeightProperty().bind(major.heightProperty().multiply(heightRatio)) }

        findComponent(WorkspacePanel::class.java, 1).sizePropertyBind(major, widthRatio, heightRatio)
        findComponent(CommandExecutor::class.java, 1).sizePropertyBind(major, widthRatio, if (heightRatio == null) null else heightRatio * 0.3)
    }

    override fun actionAfterShow() {
    }

    init {
        parser.parse(this::class.java)
    }
}