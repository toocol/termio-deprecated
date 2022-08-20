package com.toocol.termio.utilities.escape

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/9 17:02
 */
enum class EscapeKeyBoardStringMode : IEscapeMode {
    KEY_BOARD_STRING_MODE;

    var code: String? = null
        private set
    var string: String? = null
        private set

    fun setCode(code: String?): EscapeKeyBoardStringMode {
        this.code = code
        return this
    }

    fun setString(string: String?): EscapeKeyBoardStringMode {
        this.string = string
        return this
    }

    companion object {
        private const val comment = """
            The codes in parentheses are not available on some keyboards. ANSI.SYS will not interpret the codes in parentheses for those keyboards unless you specify the /X switch in the DEVICE command for ANSI.SYS.
            String is either the ASCII code for a single character or a string contained in quotation marks. For example, both 65 and "A" can be used to represent an uppercase A.
            Some of the codes are not valid for all computers. Check your computer's documentation for values that are different."""
    }
}