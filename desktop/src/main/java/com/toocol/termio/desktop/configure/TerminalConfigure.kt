package com.toocol.termio.desktop.configure

import com.toocol.termio.utilities.config.Configure
import com.toocol.termio.utilities.config.SubConfigure
import org.ini4j.Profile

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/4 18:01
 */
class TerminalConfigure : Configure() {
    override fun section(): String {
        return "terminal"
    }

    override fun assemble(section: Profile.Section) {}

    internal class TerminalFontConfigure : SubConfigure() {
        override fun section(): String {
            return "terminal.font"
        }

        override fun assemble(section: Profile.Section) {}
    }

    internal class TerminalWindowConfigure : SubConfigure() {
        override fun section(): String {
            return "terminal.window"
        }

        override fun assemble(section: Profile.Section) {}
    }
}