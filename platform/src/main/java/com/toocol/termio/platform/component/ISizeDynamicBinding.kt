package com.toocol.termio.platform.component

import javafx.scene.layout.Pane

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/25 11:55
 * @version: 0.0.1
 */
interface ISizeDynamicBinding {

    fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?)

}