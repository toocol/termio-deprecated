package com.toocol.termio.desktop.components.terminal.config

import com.toocol.termio.utilities.config.ConfigInstance

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/28 23:06
 * @version: 0.0.1
 */
class TerminalConfig : ConfigInstance() {
    class Color : ConfigInstance(){
        override fun clazz(): Class<out ConfigInstance> {
            return Color::class.java
        }
    }

    class Font : ConfigInstance() {
        override fun clazz(): Class<out ConfigInstance> {
            return Font::class.java
        }
    }

    class Window : ConfigInstance() {
        override fun clazz(): Class<out ConfigInstance> {
            return Window::class.java
        }
    }

    override fun clazz(): Class<out ConfigInstance> {
        return TerminalConfig::class.java
    }
}