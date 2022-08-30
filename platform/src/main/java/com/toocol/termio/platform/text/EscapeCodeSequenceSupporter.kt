package com.toocol.termio.platform.text

import com.toocol.termio.utilities.escape.AnsiEscapeAction
import com.toocol.termio.utilities.escape.IEscapeMode
import org.fxmisc.richtext.MultiChangeBuilder

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/16 23:54
 * @version: 0.0.1
 */
interface EscapeCodeSequenceSupporter<T> {
    fun createMultiChangeBuilder(): MultiChangeBuilder<*, *, *>

    fun getActionMap(): Map<Class<out IEscapeMode>, AnsiEscapeAction<T>>?

    fun collectReplacement(text: String, multiChangeConst: MultiChangeBuilder<*, *, *>)
}