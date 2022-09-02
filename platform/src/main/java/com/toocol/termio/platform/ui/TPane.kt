package com.toocol.termio.platform.ui

import com.toocol.termio.platform.component.IActionAfterShow
import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.component.IStyleAble
import javafx.scene.layout.Pane

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/11 10:37
 */
abstract class TPane(protected val id: Long) : Pane(), IComponent, IStyleAble, IActionAfterShow {
    init {
        this.registerComponent(id)
    }

    override fun id(): Long {
        return id
    }
}