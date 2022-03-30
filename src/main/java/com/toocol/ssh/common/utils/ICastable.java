package com.toocol.ssh.common.utils;

/**
 * 用于强制类型转换
 *
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2021/10/28 14:49
 */
public interface ICastable {
    /**
     * Automatically deduces type conversions, ignoring "unchecked" warnings
     *
     * @param obj 需要转换的对象
     * @return 强制类型转换后的对象
     */
    @SuppressWarnings("unchecked")
    default <T> T cast(Object obj) {
        if (obj == null) {
            return null;
        }
        return (T) obj;
    }
}
