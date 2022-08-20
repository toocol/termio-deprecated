package com.toocol.termio.utilities.escape

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/7 22:23
 * @version: 0.0.1
 */
enum class EscapeCommonPrivateMode(val code: String, val desc: String) : IEscapeMode {
    CURSOR_INVISIBLE("25l", "make cursor invisible."),
    CURSOR_VISIBLE("25h", "make cursor visible."),
    RESTORE_SCREEN("47l", "restore screen."),
    SAVE_SCREEN("47h", "save screen."),
    ENABLE_ALTERNATIVE_BUFFER("1049h", "enables the alternative buffer."),
    DISABLE_ALTERNATIVE_BUFFER("1049l", "disables the alternative buffer.");

    companion object {
        fun codeOf(code: String): EscapeCommonPrivateMode? {
            for (mode in values()) {
                if (mode.code == code) {
                    return mode
                }
            }
            return null
        }
    }
}