package com.toocol.termio.platform.ui

import com.toocol.termio.platform.component.IActionAfterShow
import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.component.IStyleAble
import javafx.scene.layout.BorderPane

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/11 11:04
 */
abstract class TBorderPane(protected val id: Long) : BorderPane(), IComponent, IStyleAble, IActionAfterShow {
    init {
        this.registerComponent(id)
    }

    override fun id(): Long {
        return id
    }
}