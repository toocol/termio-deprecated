package com.toocol.termio.utilities.config;

import org.ini4j.Ini;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/3 16:06
 */
class IniConfigLoaderTest {

    @Test
    void read() {
        IniConfigLoader.setConfigFileRootPath("../config");
        assertDoesNotThrow(() -> {
            Collection<Ini> inis = IniConfigLoader.read();
            assertFalse(inis.isEmpty());
        });
    }

    @Test
    void load() {
        IniConfigLoader.setConfigFileRootPath("../config");
        IniConfigLoader.setConfigurePaths(new String[] {"com.toocol.termio.core.config.core"});
        IniConfigLoader.loadConfig();
    }

}