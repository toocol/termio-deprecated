package com.toocol.termio.core.config.core

import com.toocol.termio.core.config.core.TermThemeConfig.*
import com.toocol.termio.utilities.config.Configure
import com.toocol.termio.utilities.config.SubConfigure
import com.toocol.termio.utilities.log.Loggable
import org.ini4j.Profile

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/3 15:17
 */
class TermThemeConfigure : Configure<TermThemeConfig>(), Loggable {
    override fun section(): String {
        return "theme"
    }

    override fun assemble(section: Profile.Section) {}
    internal class DarkThemeSubConfigure : SubConfigure<DarkTheme>() {
        override fun section(): String {
            return "theme.dark"
        }

        override fun assemble(section: Profile.Section) {}
    }

    internal class GruvboxThemeSubConfigure : SubConfigure<GruvboxTheme>() {
        override fun section(): String {
            return "theme.gruvbox"
        }

        override fun assemble(section: Profile.Section) {}
    }

    internal class LightThemeSubConfigure : SubConfigure<LightTheme>() {
        override fun section(): String {
            return "theme.light"
        }

        override fun assemble(section: Profile.Section) {}
    }
}