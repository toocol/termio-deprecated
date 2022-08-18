package com.toocol.termio.utilities.config

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/3 16:06
 */
internal class IniConfigLoaderTest {
    @Test
    fun read() {
        IniConfigLoader.setConfigFileRootPath("../config")
        Assertions.assertDoesNotThrow {
            val inis = IniConfigLoader.read()
            Assertions.assertFalse(inis.isEmpty())
        }
    }

    @Test
    fun load() {
        IniConfigLoader.setConfigFileRootPath("../config")
        IniConfigLoader.setConfigurePaths(arrayOf("com.toocol.termio.core.config.core"))
        IniConfigLoader.loadConfig()
    }
}