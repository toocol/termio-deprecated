package com.toocol.termio.utilities.action

import com.toocol.termio.utilities.event.CharEvent

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 18:23
 */
abstract class AbstractCharAction<D : AbstractDevice> {
    /**
     * subscribe the interested char event;
     *
     * @return interested char event
     */
    abstract fun watch(): Array<CharEvent?>

    /**
     * @param device    the char event action which belongs to.
     * @param charEvent char input event.
     * @param inChar    the input char.
     * @return true: break the outside loop; false: continue
     */
    abstract fun act(device: D, charEvent: CharEvent, inChar: Char): Boolean
}