package com.toocol.termio.core.shell.core

import com.toocol.termio.utilities.action.AbstractCharAction
import com.toocol.termio.utilities.event.CharEvent

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:10
 */
abstract class ShellCharAction : AbstractCharAction<Shell>() {

    override fun act(device: Shell, charEvent: CharEvent, inChar: Char): Boolean {
        return actOn(device, charEvent, inChar)
    }

    abstract fun actOn(shell: Shell, charEvent: CharEvent, inChar: Char): Boolean

    /**
     * reset the action;
     */
    companion object {
        /**
         * record the local input string in this read loop.
         */
        @JvmField
        internal val localLastInputBuffer = StringBuilder()

        /**
         * remote cursor position has changed sign.
         */
        @JvmField
        internal var remoteCursorOffset = false

        @JvmStatic
        fun reset() {
            localLastInputBuffer.delete(0, localLastInputBuffer.length)
            remoteCursorOffset = false
        }
    }
}