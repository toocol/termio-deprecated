package com.toocol.ssh.utilities.utils;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/7 15:53
 */
public class ClassLoaderUtil {
    /**
     * Get {@link ClassLoader}<br>
     * Get as following sequence：<br>
     *
     * <pre>
     * 1.Get current thread's ContextClassLoader
     * 2.Get current class's ClassLoader
     * 3.Get system ClassLoader（{@link ClassLoader#getSystemClassLoader()}）
     * </pre>
     *
     * @return 类加载器
     */
    public static ClassLoader getClassLoader() {
        ClassLoader classLoader = getContextClassLoader();
        if (classLoader == null) {
            classLoader = ClassLoaderUtil.class.getClassLoader();
            if (null == classLoader) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
        }
        return classLoader;
    }

    public static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
