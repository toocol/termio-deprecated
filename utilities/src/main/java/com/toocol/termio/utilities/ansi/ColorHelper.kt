package com.toocol.termio.utilities.ansi

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/18 13:43
 */
object ColorHelper {
    /**
     * 256-color mode.
     */
    @JvmStatic
    fun front(msg: String, color: Int): String {
        return "\u001b[38;5;${color}m${msg}\u001b[0m"
    }
    @JvmStatic
    fun background(msg: String, color: Int): String {
        return "\u001b[48;5;${color}m${msg}\u001b[0m"
    }

    /**
     * RGB-color mode
     */
    @JvmStatic
    fun front(msg: String, r: Int, g: Int, b: Int): String {
        return "\u001b[38;2${r};${g};${b}m${msg}\u001b[0m"
    }
    @JvmStatic
    fun background(msg: String, r: Int, g: Int, b: Int): String {
        return "\u001b[48;2${r};${g};${b}m${msg}\u001b[0m"
    }
}