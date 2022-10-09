package com.toocol.termio.platform.component

import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.Asable
import com.toocol.termio.utilities.utils.Castable
import javafx.scene.Node

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/5 17:44
 */
interface IComponent : Asable, Castable, ISizeDynamicBinding, Loggable {
    /**
     * Initialize the component.
     */
    fun initialize()

    /**
     * If the component is subclass of Node, hide this component.
     */
    fun hide() {
        if (this is Node) {
            this.isManaged = false
            this.isVisible = false
        }
    }

    /**
     * If the component is subclass of Node, hide this component.
     */
    fun show() {
        if (this is Node) {
            this.isManaged = true
            this.isVisible = true
        }
    }

    fun <R> R.initialVisible(visible: Boolean): R {
        if (this is Node) {
            this.isVisible = visible
        }
        return this
    }
}