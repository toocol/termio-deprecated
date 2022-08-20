package com.toocol.termio.utilities.escape

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/9 15:18
 */
enum class EscapeColorRgbMode : IEscapeMode {
    COLOR_RGB_MODE;

    private var foreground = false
    fun setForeground(foreground: Boolean): EscapeColorRgbMode {
        this.foreground = foreground
        return this
    }
}