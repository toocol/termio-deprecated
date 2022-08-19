package com.toocol.termio.platform.ui

import javafx.scene.layout.FlowPane
import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.component.IStyleAble
import com.toocol.termio.platform.component.IActionAfterShow

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/11 11:02
 */
abstract class TFlowPane(protected val id: Long) : FlowPane(), IComponent, IStyleAble, IActionAfterShow {
    init {
        this.registerComponent(id)
    }

    override fun id(): Long {
        return id
    }
}