package com.toocol.termio.core.config.core

import com.toocol.termio.utilities.config.ConfigInstance

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/29 11:35
 */
class TermThemeConfig : ConfigInstance() {
    override fun clazz(): Class<out ConfigInstance> {
        return TermThemeConfig::class.java
    }

    class DarkTheme : ConfigInstance() {
        override fun clazz(): Class<out ConfigInstance> {
            return DarkTheme::class.java
        }
    }

    class GruvboxTheme : ConfigInstance() {
        override fun clazz(): Class<out ConfigInstance> {
            return GruvboxTheme::class.java
        }
    }

    class LightTheme : ConfigInstance() {
        override fun clazz(): Class<out ConfigInstance> {
            return LightTheme::class.java
        }
    }
}