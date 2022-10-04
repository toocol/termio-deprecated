package com.toocol.termio.utilities.log

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/28 11:55
 */
interface Logger {
    fun debug(className: String, message: String?, vararg params: Any?)

    fun info(className: String, message: String?, vararg params: Any?)

    fun warn(className: String, message: String?, vararg params: Any?)

    fun error(className: String, message: String?, vararg params: Any?)
}