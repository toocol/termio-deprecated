package com.toocol.termio.utilities.utils

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/2 14:37
 * @version: 0.0.1
 */
class TimeRecorder {
    var start: Long = 0
    var end: Long = 0

    fun end(): Long {
        end = System.currentTimeMillis()
        return end - start
    }

    init {
        start = System.currentTimeMillis()
    }
}