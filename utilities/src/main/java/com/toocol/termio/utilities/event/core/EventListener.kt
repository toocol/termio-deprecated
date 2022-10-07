package com.toocol.termio.utilities.event.core

import com.toocol.termio.utilities.utils.Asable
import kotlin.reflect.KClass

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 16:20
 * @version: 0.0.1
 */
abstract class EventListener<T : AbstractEvent> : Asable{

    abstract fun watch() : KClass<T>

    abstract fun actOn(event : T)

}