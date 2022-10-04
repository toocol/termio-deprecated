package com.toocol.termio.platform.text

import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.utilities.log.Loggable
import javafx.scene.layout.Pane
import kotlin.math.max

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/14 18:21
 * @version: 0.0.1
 */
open class Cursor(val id: Long) : IComponent, Loggable {
    /**
     * This is the position which text should be inserted at.
     */
    var inlinePosition = 0
        internal set

    init {
        this.registerComponent(id)
    }

    override fun initialize() {}

    override fun id(): Long {
        return id
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {

    }

    fun moveLeft(value: Int) {
        inlinePosition = max(inlinePosition - value, 0)
    }

    fun moveRight(value: Int) {
        inlinePosition += value
    }

    fun update(value: Int) {
        inlinePosition = max(inlinePosition + value, 0)
    }

    fun setTo(value: Int) {
        inlinePosition = value
    }
}