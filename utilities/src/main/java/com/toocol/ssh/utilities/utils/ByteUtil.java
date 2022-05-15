package com.toocol.ssh.utilities.utils;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/15 20:50
 * @version: 0.0.1
 */
public final class ByteUtil {

    public static boolean equals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

}
