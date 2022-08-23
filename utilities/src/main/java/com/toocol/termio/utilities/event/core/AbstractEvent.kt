package com.toocol.termio.utilities.event.core

import com.toocol.termio.utilities.utils.Asable

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 16:18
 * @version: 0.0.1
 */
abstract class AbstractEvent : Asable{
    fun dispatch() {
        EventDispatcher.dispatch(this)
    }
}