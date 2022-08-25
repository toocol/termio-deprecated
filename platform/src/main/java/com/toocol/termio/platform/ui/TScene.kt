package com.toocol.termio.platform.ui

import javafx.scene.Scene
import com.toocol.termio.platform.component.IComponent
import javafx.scene.Parent
import javafx.scene.layout.Pane

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/11 12:00
 */
open class TScene(protected val id: Long, root: Parent?) : Scene(root), IComponent {
    init {
        this.registerComponent(id)
    }

    override fun initialize() {}

    override fun id(): Long {
        return id
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {

    }
}