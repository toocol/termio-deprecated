package com.toocol.termio.desktop.components.panel.ui

import com.toocol.termio.desktop.components.executor.ui.CommandExecutor
import com.toocol.termio.platform.component.Component
import com.toocol.termio.platform.component.ComponentsParser
import com.toocol.termio.platform.component.RegisterComponent
import com.toocol.termio.platform.ui.TBorderPane

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 12:46
 * @version: 0.0.1
 */
@RegisterComponent(value = [
    Component(clazz = WorkspacePanel::class, id = 1, initialVisible = true),
    Component(clazz = CommandExecutor::class, id = 1, initialVisible = true)
])
class CenterPanel(id: Long) : TBorderPane(id) {

    private val parser = ComponentsParser()

    override fun styleClasses(): Array<String> {
        return arrayOf(

        )
    }

    override fun initialize() {
        styled()
        val majorPanel = findComponent(MajorPanel::class.java, 1)
        maxHeightProperty().bind(majorPanel.heightProperty())
        maxWidthProperty().bind(majorPanel.widthProperty())
        prefHeightProperty().bind(majorPanel.heightProperty())
        prefWidthProperty().bind(majorPanel.widthProperty())

        parser.parse(CenterPanel::class.java)
        parser.initializeAll()

        center = parser.get(WorkspacePanel::class.java)
        bottom = parser.get(CommandExecutor::class.java)
    }

    override fun actionAfterShow() {

    }
}