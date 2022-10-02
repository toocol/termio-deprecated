package com.toocol.coroutines

import com.toocol.coroutines.core.ApiAcquirer
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.layout.AnchorPane
import javafx.scene.text.Text
import javafx.stage.Stage
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx

class CoroutinesApplication : Application(), ApiAcquirer, CoroutineScope by MainScope() {

    @DelicateCoroutinesApi
    override fun start(stage: Stage) {
        AppModuleBootstrap.bootstrap()

        val text = Text("Place to show user")
        val pane = AnchorPane().apply {
            children.add(text)
            AnchorPane.setTopAnchor(text, 10.0)
            AnchorPane.setLeftAnchor(text, 10.0)

            onMouseClicked = EventHandler { println("Clicked!") }
        }

        val scene = Scene(pane, 320.0, 240.0)
        stage.title = "Javafx Coroutines"
        stage.scene = scene

        println("Start: ${Thread.currentThread().name}")

        Thread {
            println("Thread: ${Thread.currentThread().name}")
            launch(Dispatchers.JavaFx) {
                delay(1000)
                println("Launch: ${Thread.currentThread().name}")
                val user = api(UserApi, Dispatchers.IO) {
                    getUser()
                }
                api(UserApi, Dispatchers.JavaFx) {
                    showUser(text, user)
                }
            }
        }.start()

        stage.show()
    }
}

/**
 * There were some problems when run application directly, please 'run' with gradle.
 */
fun main() {
    Application.launch(CoroutinesApplication::class.java)
}