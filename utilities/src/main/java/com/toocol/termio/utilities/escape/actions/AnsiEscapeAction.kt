package com.toocol.termio.utilities.escape.actions

import com.toocol.termio.utilities.utils.Asable
import com.toocol.termio.utilities.escape.IEscapeMode

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/7 18:45
 * @version: 0.0.1
 */
abstract class AnsiEscapeAction<T> : Asable {
    abstract fun focusMode(): Class<out IEscapeMode>
    abstract fun action(executeTarget: T, escapeMode: IEscapeMode, params: List<Any>)
}