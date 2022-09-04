package com.toocol.termio.platform.font

import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.text.Font

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/3 1:15
 * @version: 0.0.1
 */
class FluentIcon(text: String) : Label(text) {
    fun setSize(pixelSize: Double, prefWidth: Double = pixelSize, prefHeight: Double = pixelSize) {
        val font = font
        val newFont = Font(font.name, pixelSize)
        setFont(newFont)
        setPrefSize(prefWidth, prefHeight)
    }

    init {
        setMinSize(USE_PREF_SIZE, USE_PREF_SIZE)
        alignment = Pos.CENTER
        styleClass.addAll("icon-font", "fluent-assets")
    }
}