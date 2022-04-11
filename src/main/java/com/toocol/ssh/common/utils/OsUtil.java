package com.toocol.ssh.common.utils;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/11 18:01
 */
public class OsUtil {
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
}
