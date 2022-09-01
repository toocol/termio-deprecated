package com.toocol.termio.desktop.bootstrap

import com.toocol.termio.desktop.components.panel.ui.LoadingPanel
import javafx.application.Preloader
import javafx.stage.Stage

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/2 0:25
 * @version: 0.0.1
 */
class TermioPreloader : Preloader() {

    private var loadingPanel: LoadingPanel? = LoadingPanel()

    override fun start(primaryStage: Stage?) {
        loadingPanel?.loading()
        println("preload")
    }

    override fun handleProgressNotification(info: ProgressNotification) {
    }

    override fun handleStateChangeNotification(info: StateChangeNotification) {
        println(info.type)
    }

    override fun handleApplicationNotification(info: PreloaderNotification) {
        takeIf { info is ApplicationStartupNotification }.run {
            loadingPanel?.close()
            loadingPanel = null
        }
    }

    override fun handleErrorNotification(info: ErrorNotification): Boolean {
        return false
    }

    class ApplicationStartupNotification : PreloaderNotification
}