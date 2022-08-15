package com.toocol.termio.starter;

import com.toocol.termio.core.Termio;
import com.toocol.termio.core.term.vert.TermDesktopVerticle;
import com.toocol.termio.utilities.functional.Ignore;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 15:00
 */
@Ignore(ignore = TermDesktopVerticle.class)
public class TermioApplication {

    public static void main(String[] args) {
        Termio.runConsole(TermioApplication.class);
    }

}
