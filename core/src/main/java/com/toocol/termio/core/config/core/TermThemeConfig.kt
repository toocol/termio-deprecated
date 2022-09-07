package com.toocol.termio.core.config.core;

import com.toocol.termio.utilities.config.ConfigInstance;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/29 11:35
 */
public class TermThemeConfig extends ConfigInstance {

    @NotNull
    @Override
    public Class<? extends ConfigInstance> clazz() {
        return TermThemeConfig.class;
    }

    public static class DarkTheme extends ConfigInstance {
        @NotNull
        @Override
        public Class<? extends ConfigInstance> clazz() {
            return DarkTheme.class;
        }
    }

    public static class GruvboxTheme extends ConfigInstance {
        @NotNull
        @Override
        public Class<? extends ConfigInstance> clazz() {
            return GruvboxTheme.class;
        }
    }

    public static class LightTheme extends ConfigInstance {
        @NotNull
        @Override
        public Class<? extends ConfigInstance> clazz() {
            return LightTheme.class;
        }
    }
}
