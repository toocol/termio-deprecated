package com.toocol.termio.utilities.escape

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/27 22:23
 * @version: 0.0.1
 */
class EscapeColorGraphicsCombine : IEscapeMode{
    val list: MutableList<IEscapeMode> = mutableListOf()

    fun add(mode: IEscapeMode) {
        list.add(mode)
    }

    companion object {
        fun get(): EscapeColorGraphicsCombine {
            return EscapeColorGraphicsCombine()
        }
    }
}