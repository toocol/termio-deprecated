package com.toocol.termio.desktop.components.panel.ui

import javafx.scene.Scene
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.lang.ref.WeakReference

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/30 23:25
 * @version: 0.0.1
 */
class LoadingPanel {
    private var loadingPane: WeakReference<AnchorPane>? = WeakReference(AnchorPane())
    private var loadingScene: WeakReference<Scene>? = WeakReference(Scene(loadingPane!!.get()))
    private var loadingStage: WeakReference<Stage>? = WeakReference(Stage())

    fun loading() {
        loadingPane!!.get()!!.setPrefSize(400.0, 250.0)
        loadingStage!!.get()!!.initStyle(StageStyle.UTILITY)
        loadingStage!!.get()!!.scene = loadingScene!!.get()
        loadingStage!!.get()!!.show()
    }

    fun close() {
        loadingStage!!.get()!!.close()
        loadingPane = null
        loadingScene = null
        loadingStage = null
    }
}