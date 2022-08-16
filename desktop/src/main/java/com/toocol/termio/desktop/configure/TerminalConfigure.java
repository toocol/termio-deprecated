package com.toocol.termio.desktop.configure;

import com.toocol.termio.utilities.config.Configure;
import com.toocol.termio.utilities.config.SubConfigure;
import org.ini4j.Profile;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/4 18:01
 */
public class TerminalConfigure extends Configure {
    @Override
    public String section() {
        return "terminal";
    }

    @Override
    public void assemble(Profile.Section section) {

    }

    static class TerminalFontConfigure extends SubConfigure {

        @Override
        public String section() {
            return "terminal.font";
        }

        @Override
        public void assemble(Profile.Section section) {

        }
    }

    static class TerminalWindowConfigure extends SubConfigure {

        @Override
        public String section() {
            return "terminal.window";
        }

        @Override
        public void assemble(Profile.Section section) {

        }
    }
}
