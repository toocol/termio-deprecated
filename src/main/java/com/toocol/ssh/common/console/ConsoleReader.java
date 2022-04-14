package com.toocol.ssh.common.console;

import com.toocol.ssh.common.utils.OsUtil;

import java.io.InputStream;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/14 16:52
 */
public abstract class ConsoleReader implements ConsoleOperations{

    protected final InputStream in;

    protected ConsoleReader(InputStream in) {
        this.in = in;
    }

    public static ConsoleReader get(InputStream in) {
        if (OsUtil.isWindows()) {
            return new WindowsConsoleReader(in);
        } else {
            return new UnixConsoleReader(in);
        }
    }

   public abstract int readVirtualKey();

}
