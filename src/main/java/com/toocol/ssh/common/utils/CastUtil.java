package com.toocol.ssh.common.utils;

/**
 * @author Joezeo
 * @date 2020/12/6 16:27
 */
public class CastUtil {
    /**
     * 自动推导类型转换，忽视 unchecked 警告
     *
     * @param obj
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        if (obj == null) {
            return null;
        }
        return (T)obj;
    }
}
