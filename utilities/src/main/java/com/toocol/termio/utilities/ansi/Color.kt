package com.toocol.termio.utilities.ansi

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/2 14:32
 */
class Color(@JvmField val shortcut: String, @JvmField var color: Int, @JvmField val name: String) {
    companion object {
        @JvmStatic
        fun of(shortcut: String, color: Int, name: String): Color {
            return Color(shortcut, color, name)
        }
    }
}