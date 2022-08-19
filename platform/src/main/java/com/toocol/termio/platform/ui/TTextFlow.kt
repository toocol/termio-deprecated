package com.toocol.termio.platform.ui

import com.toocol.termio.platform.component.IActionAfterShow
import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.component.IStyleAble
import javafx.scene.text.TextFlow

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/12 14:28
 */
abstract class TTextFlow(protected val id: Long) : TextFlow(), IComponent, IStyleAble, IActionAfterShow {
    init {
        this.registerComponent(id)
    }

    override fun initialize() {}
    override fun id(): Long {
        return id
    }
}