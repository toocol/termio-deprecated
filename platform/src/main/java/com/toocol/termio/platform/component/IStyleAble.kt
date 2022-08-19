package com.toocol.termio.platform.component

import javafx.scene.Node

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/11 14:00
 */
interface IStyleAble {
    fun styleClasses(): Array<String>

    /**
     * add styles to components
     */
    fun styled() {
        if (this !is Node) {
            return
        }
        (this as Node).styleClass.addAll(*styleClasses())
    }
}