package com.toocol.termio.core.config.core;

import com.toocol.termio.utilities.config.Configure;
import com.toocol.termio.utilities.config.SubConfigure;
import com.toocol.termio.utilities.log.Loggable;
import org.ini4j.Profile;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/3 15:17
 */
public class TermThemeConfigure extends Configure<TermThemeConfig> implements Loggable {

    @Override
    @NotNull
    public String section() {
        return "theme";
    }

    @Override
    public void assemble(@NotNull Profile.Section section) {

    }

    static class DarkThemeSubConfigure extends SubConfigure<TermThemeConfig.DarkTheme> {
        @Override
        @NotNull
        public String section() {
            return "theme.dark";
        }

        @Override
        public void assemble(@NotNull Profile.Section section) {

        }
    }

    static class GruvboxThemeSubConfigure extends SubConfigure<TermThemeConfig.GruvboxTheme> {
        @NotNull
        @Override
        public String section() {
            return "theme.gruvbox";
        }

        @Override
        public void assemble(@NotNull Profile.Section section) {

        }
    }

    static class LightThemeSubConfigure extends SubConfigure<TermThemeConfig.LightTheme> {
        @NotNull
        @Override
        public String section() {
            return "theme.light";
        }

        @Override
        public void assemble(@NotNull Profile.Section section) {

        }
    }
}
