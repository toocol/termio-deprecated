package com.toocol.termio.platform.font

import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.text.Font

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/2 18:00
 * @version: 0.0.1
 */
class FontAwesomeIcon(text: String, type: Type) : Label(text) {

    fun setSize(pixelSize: Double, prefWidth: Double = pixelSize, prefHeight: Double = pixelSize) {
        val font = font
        val newFont = Font(font.name, pixelSize)
        setFont(newFont)
        setPrefSize(prefWidth, prefHeight)
    }

    init {
        setMinSize(USE_PREF_SIZE, USE_PREF_SIZE)
        alignment = Pos.CENTER
        when (type) {
            Type.REGULAR -> styleClass.addAll("fa-regular", "font-awesome-regular-assets")
            Type.SOLID -> styleClass.addAll("fa-solid", "font-awesome-solid-assets")
            Type.BRAND -> styleClass.addAll("fa-brand", "font-awesome-brand-assets")
        }
    }

    enum class Type {
        REGULAR, SOLID, BRAND
    }
}