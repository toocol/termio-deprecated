package com.toocol.termio.platform.ui

import com.toocol.termio.platform.component.IActionAfterShow
import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.component.IStyleAble
import javafx.scene.layout.GridPane

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/11 10:37
 */
abstract class TGridPane(protected val id: Long) : GridPane(), IComponent, IStyleAble, IActionAfterShow {
    init {
        this.registerComponent(id)
    }

    override fun id(): Long {
        return id
    }
}