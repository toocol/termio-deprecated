package com.toocol.termio.platform.ui

import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.component.IStyleAble
import com.toocol.termio.platform.component.IActionAfterShow
import javafx.scene.control.ScrollPane

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/11 22:38
 * @version: 0.0.1
 */
abstract class TScrollPane(protected val id: Long) : ScrollPane(), IComponent, IStyleAble, IActionAfterShow {
    init {
        this.registerComponent(id)
    }

    override fun id(): Long {
        return id
    }
}