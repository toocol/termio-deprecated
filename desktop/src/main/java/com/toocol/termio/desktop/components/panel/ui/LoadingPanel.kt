package com.toocol.termio.desktop.components.panel.ui

import javafx.scene.Scene
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import javafx.stage.StageStyle

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/30 23:25
 * @version: 0.0.1
 */
class LoadingPanel {
    private var loadingPane: AnchorPane? = AnchorPane()
    private var loadingScene: Scene? = Scene(loadingPane)
    private var loadingStage: Stage? = Stage()

    fun loading() {
        loadingPane?.setPrefSize(400.0, 250.0)
        loadingPane?.style = "-fx-background-color: #FFFFFF;"

        loadingStage?.initStyle(StageStyle.UNDECORATED)
        loadingStage?.scene = loadingScene
        loadingStage?.toFront()
        loadingStage?.show()
    }

    fun close() {
        loadingStage?.close()
        loadingPane = null
        loadingScene = null
        loadingStage = null
    }
}