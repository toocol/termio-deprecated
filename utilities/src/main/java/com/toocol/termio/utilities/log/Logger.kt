package com.toocol.termio.utilities.log

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/28 11:55
 */
interface Logger {
    fun debug(message: String?, vararg params: Any?)

    fun info(message: String?, vararg params: Any?)

    fun warn(message: String?, vararg params: Any?)

    fun error(message: String?, vararg params: Any?)
}