package com.toocol.termio.utilities.escape

/**
 * Operating System Commands
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/26 15:21
 * @version: 0.0.1
 */
enum class EscapeOSCMode(val code: Int, val comment: String, val sequence: String) : IEscapeMode{
    RENAMING_TAB_TITLE_0(0, "The custom tab title will be displayed within the parameter pt", "ESC]0;ptBEL"),
    RENAMING_TAB_TITLE_1(1, "The custom tab title will be displayed within the parameter pt", "ESC]1;ptBEL"),
    RENAMING_WIDOW_TITLE(2, "The custom window title will be displayed within the parameter pt", "ESC]2;ptBEL"),
    ECHO_WORKING_DOCUMENT(6, "Update the prompt to echo the current working document", "ESC]6;ptBEL"),
    ECHO_WORKING_DIRECTORY(7, "Update the prompt to echo the current working directory", "ESC]7;ptBEL"),
    ;

    companion object {
        fun codeOf(code: Int): EscapeOSCMode? {
            for (value in values()) {
                if (value.code == code) {
                    return value
                }
            }
            return null
        }
    }
}